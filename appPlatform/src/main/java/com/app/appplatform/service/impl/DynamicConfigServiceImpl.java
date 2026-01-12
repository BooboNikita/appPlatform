package com.app.appplatform.service.impl;

import com.app.appplatform.entity.DynamicConfig;
import com.app.appplatform.enums.BucketType;
import com.app.appplatform.mapper.DynamicConfigMapper;
import com.app.appplatform.service.DynamicConfigService;
import com.app.appplatform.service.MinioService;
import com.app.appplatform.util.JsonUtil;
import com.app.appplatform.util.VersionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DynamicConfigServiceImpl implements DynamicConfigService {

    @Autowired
    private DynamicConfigMapper dynamicConfigMapper;

    @Autowired
    private MinioService minioService;

    @Override
    public DynamicConfig uploadConfig(MultipartFile file, String versionRange, String env, String remark) throws Exception {
        // 1. 上传文件到 MinIO
        String fileName = UUID.randomUUID().toString() + ".json";
        String fileUrl = minioService.uploadFile(file, fileName, BucketType.DYNAMIC_CONFIG);

        // 2. 保存元数据到数据库
        DynamicConfig dynamicConfig = new DynamicConfig();
        dynamicConfig.setVersionRange(versionRange);
        dynamicConfig.setFileUrl(fileUrl);
        dynamicConfig.setEnv(env);
        dynamicConfig.setRemark(remark);
        
        dynamicConfigMapper.insert(dynamicConfig);
        return dynamicConfig;
    }

    @Override
    public DynamicConfig getConfigById(Long id) {
        return dynamicConfigMapper.findById(id);
    }

    @Override
    public List<DynamicConfig> getAllConfigs(String env) {
        return dynamicConfigMapper.findAll(env);
    }

    @Override
    public DynamicConfig getLatestConfigByVersion(String version, String env) {
        // 获取匹配环境的所有配置，已按时间倒序排列
        List<DynamicConfig> matchedConfigs = dynamicConfigMapper.findByEnv(env);
        
        // 使用 VersionUtil 进行语义化匹配，返回第一条匹配的记录（即最新的）
        return matchedConfigs.stream()
                .filter(config -> VersionUtil.isMatch(version, config.getVersionRange()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void deleteConfig(Long id) throws Exception {
        DynamicConfig config = dynamicConfigMapper.findById(id);
        if (config != null) {
            // 1. 从 MinIO 删除文件
            String fileName = config.getFileUrl().substring(config.getFileUrl().lastIndexOf("/") + 1);
            minioService.deleteFile(fileName, BucketType.DYNAMIC_CONFIG);
            
            // 2. 从数据库删除记录
            dynamicConfigMapper.deleteById(id);
        }
    }

    @Override
    public DynamicConfig updateConfig(Long id, MultipartFile file, String versionRange, String env, String remark) throws Exception {
        DynamicConfig existingConfig = dynamicConfigMapper.findById(id);
        if (existingConfig == null) {
            throw new RuntimeException("配置不存在");
        }

        if (file != null && !file.isEmpty()) {
            // 删除旧文件
            String oldFileName = existingConfig.getFileUrl().substring(existingConfig.getFileUrl().lastIndexOf("/") + 1);
            minioService.deleteFile(oldFileName, BucketType.DYNAMIC_CONFIG);
            
            // 上传新文件
            String newFileName = UUID.randomUUID().toString() + ".json";
            String newFileUrl = minioService.uploadFile(file, newFileName, BucketType.DYNAMIC_CONFIG);
            existingConfig.setFileUrl(newFileUrl);
        }

        if (versionRange != null) {
            existingConfig.setVersionRange(versionRange);
        }
        if (env != null) {
            existingConfig.setEnv(env);
        }
        if (remark != null) {
            existingConfig.setRemark(remark);
        }

        dynamicConfigMapper.update(existingConfig);
        return existingConfig;
    }

    @Override
    public String getConfigContent(Long id) throws Exception {
        DynamicConfig config = dynamicConfigMapper.findById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        // 从 URL 中提取文件名
        String fileName = config.getFileUrl().substring(config.getFileUrl().lastIndexOf("/") + 1);
        
        try (InputStream inputStream = minioService.downloadFile(fileName, BucketType.DYNAMIC_CONFIG);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String fullContent = reader.lines().collect(Collectors.joining("\n"));
            return JsonUtil.minify(fullContent);
        }
    }
}
