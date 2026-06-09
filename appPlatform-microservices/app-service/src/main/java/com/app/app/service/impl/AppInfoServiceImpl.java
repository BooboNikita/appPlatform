package com.app.app.service.impl;

import com.app.app.dto.AppInfoDto;
import com.app.app.dto.AppVersionCheckDto;
import com.app.app.entity.AppInfo;
import com.app.app.mapper.AppInfoMapper;
import com.app.app.service.AppInfoService;
import com.app.common.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AppInfoServiceImpl implements AppInfoService {

    private final AppInfoMapper appInfoMapper;
    private final ConcurrentHashMap<String, Boolean> updateStatusCache = new ConcurrentHashMap<>();

    @Autowired
    public AppInfoServiceImpl(AppInfoMapper appInfoMapper) {
        this.appInfoMapper = appInfoMapper;
        // 默认开启APP更新
        updateStatusCache.put("app_update_total_enabled", true);
    }

    @Override
    public AppInfo uploadApp(MultipartFile file, AppInfo appInfo) throws IOException {
        // 设置文件大小
        appInfo.setSize(formatFileSize(file.getSize()));
        appInfo.setCreateTime(new Date());
        appInfo.setDownloadTimes(0);
        appInfo.setDeleted(false);
        appInfo.setShowUpdatePopup(false);
        appInfo.setForceUpdate(false);

        // TODO: 上传文件到MinIO并设置path
        // 这里需要集成MinIO客户端

        appInfoMapper.insert(appInfo);
        return appInfo;
    }

    @Override
    public PageResult<AppInfoDto> getAllApps(int pageNum, int pageSize, String appName, String version, String buildNumber, Boolean isBeta) {
        PageHelper.startPage(pageNum, pageSize);
        List<AppInfo> list = appInfoMapper.selectByCondition(appName, version, buildNumber, isBeta);
        PageInfo<AppInfo> pageInfo = new PageInfo<>(list);

        List<AppInfoDto> dtoList = pageInfo.getList().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, pageInfo.getTotal(), pageNum, pageSize, pageInfo.getPages());
    }

    @Override
    public List<AppInfo> getAppsByPackageName(String packageName) {
        return appInfoMapper.selectByPackageName(packageName);
    }

    @Override
    public AppInfo getAppById(Integer id) {
        return appInfoMapper.selectById(id);
    }

    @Override
    public AppInfo updateAppInfo(AppInfo appInfo) {
        appInfoMapper.update(appInfo);
        return appInfoMapper.selectById(appInfo.getId());
    }

    @Override
    public boolean deleteApp(Integer id) {
        return appInfoMapper.deleteById(id) > 0;
    }

    @Override
    public int incrementDownloadCount(Integer id) {
        return appInfoMapper.incrementDownloadCount(id);
    }

    @Override
    public boolean existsByPackageAndVersion(String packageName, String version) {
        return appInfoMapper.selectByPackageAndVersion(packageName, version) != null;
    }

    @Override
    public AppVersionCheckDto checkVersionUpdate(HttpHeaders headers) {
        // TODO: 实现版本检查逻辑
        AppVersionCheckDto dto = new AppVersionCheckDto();
        dto.setHasUpdate(false);
        return dto;
    }

    @Override
    public ResponseEntity<StreamingResponseBody> downloadAppFile(AppInfo appInfo, String rangeHeader) {
        // TODO: 实现文件下载逻辑，集成MinIO
        throw new UnsupportedOperationException("文件下载功能待实现");
    }

    @Override
    public boolean getUpdateTotalStatus() {
        return updateStatusCache.getOrDefault("app_update_total_enabled", true);
    }

    @Override
    public void setUpdateTotalStatus(boolean enabled) {
        updateStatusCache.put("app_update_total_enabled", enabled);
    }

    private AppInfoDto convertToDto(AppInfo appInfo) {
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
        dto.setShowUpdatePopup(appInfo.getShowUpdatePopup());
        dto.setForceUpdate(appInfo.getForceUpdate());
        return dto;
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
