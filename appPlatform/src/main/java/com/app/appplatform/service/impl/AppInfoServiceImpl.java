package com.app.appplatform.service.impl;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.dto.AppInfoDto;
import com.app.appplatform.entity.AppInfo;
import com.app.appplatform.enums.BucketType;
import com.app.appplatform.mapper.primary.AppInfoMapper;
import com.app.appplatform.service.AppInfoService;
import com.app.appplatform.service.MinioService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppInfoServiceImpl implements AppInfoService {

    private final AppInfoMapper appInfoMapper;

    private final MinioService minioService;

    @Value("${app.upload.dir}/app")
    private String uploadDir;

    @Value("${minio.bucket.apps}")
    private String appsBucketName;

    public AppInfoServiceImpl(AppInfoMapper appInfoMapper, MinioService minioService) {
        this.appInfoMapper = appInfoMapper;
        this.minioService = minioService;
    }

    /**
     * 将 AppInfo 转换为 AppInfoDto
     * @param appInfo AppInfo 对象
     * @return 转换后的 AppInfoDto 对象
     */
    private AppInfoDto convertToAppInfoDto(AppInfo appInfo) {
        if (appInfo == null) {
            return null;
        }
        
        AppInfoDto dto = new AppInfoDto();
        dto.setId(appInfo.getId());
        dto.setAppName(appInfo.getAppName());
        dto.setPackageName(appInfo.getPackageName());
        dto.setVersion(appInfo.getVersion());
        dto.setBuildNumber(appInfo.getBuildNumber());
        dto.setFeatures(appInfo.getFeatures());
        dto.setIsBeta(appInfo.getIsBeta());
        dto.setCreateTime(appInfo.getCreateTime());
        dto.setDownloadTimes(appInfo.getDownloadTimes());
        dto.setSize(appInfo.getSize());
        
        return dto;
    }

    @Override
    public AppInfo uploadApp(MultipartFile file, AppInfo appInfo) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        // 检查文件大小
        long fileSize = file.getSize();
        String fileSizeStr = formatFileSize(fileSize);

        // 生成唯一的文件名
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex);
        }
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            String filePath = minioService.uploadFile(file, newFilename, BucketType.APPS);
            // 设置应用信息
            appInfo.setPath(filePath);
            appInfo.setSize(fileSizeStr);
            appInfo.setDownloadTimes(0);

            // 保存到数据库
            appInfoMapper.insert(appInfo);
            return appInfo;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<AppInfoDto> getAllApps(int pageNum, int pageSize, String appName, String version, String buildNumber, Boolean isBeta) {
        // 设置分页参数
        PageHelper.startPage(pageNum, pageSize);

        try {
            // 执行查询
            List<AppInfo> listInfo = appInfoMapper.findAllWithFilters(appName, version, buildNumber, isBeta);

            // 使用PageInfo获取分页信息
            PageInfo<AppInfo> pageInfo = new PageInfo<>(listInfo);

            // 转换为 DTO 列表
            List<AppInfoDto> dtoList = pageInfo.getList().stream()
                    .map(this::convertToAppInfoDto)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 构建并返回分页结果
            return new PageResult<>(
                    dtoList,              // 当前页数据（已转换为 DTO）
                    pageInfo.getTotal(),  // 总记录数
                    pageInfo.getPageNum(),   // 当前页码
                    pageInfo.getPageSize(),  // 每页数量
                    pageInfo.getPages()      // 总页数
            );
        } finally {
            // 清理 ThreadLocal 存储的分页参数，保证线程安全
            PageHelper.clearPage();
        }
    }

    @Override
    public List<AppInfo> getAppsByPackageName(String packageName) {
        return appInfoMapper.findByPackageName(packageName);
    }

    @Override
    public AppInfo getAppById(Integer id) {
        return appInfoMapper.findById(id);
    }

    @Override
    public boolean deleteApp(Integer id) {
        AppInfo appInfo = appInfoMapper.findById(id);
        if (appInfo != null) {
            // 删除文件
//            try {
//                Files.deleteIfExists(Paths.get(appInfo.getPath()));
//            } catch (IOException e) {
//                throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
//            }
            // 删除数据库记录
            return appInfoMapper.deleteById(id) > 0;
        }
        return false;
    }
    
    @Override
    public AppInfo updateAppInfo(AppInfo appInfo) {
        if (appInfo == null || appInfo.getId() == null) {
            throw new IllegalArgumentException("应用ID不能为空");
        }
        
        // 检查应用是否存在
        AppInfo existingApp = appInfoMapper.findById(appInfo.getId());
        if (existingApp == null) {
            throw new IllegalArgumentException("应用不存在，ID: " + appInfo.getId());
        }
        
        // 如果包名或版本号有修改，检查是否已存在相同包名和版本的应用
        if ((appInfo.getPackageName() != null && !appInfo.getPackageName().equals(existingApp.getPackageName())) ||
            (appInfo.getVersion() != null && !appInfo.getVersion().equals(existingApp.getVersion()))) {
            String newPackageName = appInfo.getPackageName() != null ? appInfo.getPackageName() : existingApp.getPackageName();
            String newVersion = appInfo.getVersion() != null ? appInfo.getVersion() : existingApp.getVersion();
            
            if (appInfoMapper.existsByPackageAndVersion(newPackageName, newVersion, appInfo.getId())) {
                throw new IllegalArgumentException("已存在相同包名和版本的应用");
            }
        }
        
        // 只更新非空字段
        if (appInfo.getAppName() != null) {
            existingApp.setAppName(appInfo.getAppName());
        }
        if (appInfo.getPackageName() != null) {
            existingApp.setPackageName(appInfo.getPackageName());
        }
        if (appInfo.getVersion() != null) {
            existingApp.setVersion(appInfo.getVersion());
        }
        if (appInfo.getBuildNumber() != null) {
            existingApp.setBuildNumber(appInfo.getBuildNumber());
        }
        if (appInfo.getFeatures() != null) {
            existingApp.setFeatures(appInfo.getFeatures());
        }
        if (appInfo.getIsBeta() != null) {
            existingApp.setIsBeta(appInfo.getIsBeta());
        }
        
        // 更新记录
        appInfoMapper.updateAppInfo(existingApp);
        
        // 返回更新后的应用信息
        return appInfoMapper.findById(appInfo.getId());
    }

    @Override
    public int incrementDownloadCount(Integer id) {
        return appInfoMapper.incrementDownloadCount(id);
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    @Override
    public boolean existsByPackageAndVersion(String packageName, String version) {
        return appInfoMapper.findByPackageAndVersion(packageName, version) != null;
    }
}
