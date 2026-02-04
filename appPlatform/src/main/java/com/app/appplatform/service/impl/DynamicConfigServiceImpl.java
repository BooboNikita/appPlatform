package com.app.appplatform.service.impl;

import com.app.appplatform.entity.DynamicConfig;
import com.app.appplatform.entity.DynamicConfigHistory;
import com.app.appplatform.enums.BucketType;
import com.app.appplatform.mapper.primary.DynamicConfigHistoryMapper;
import com.app.appplatform.mapper.primary.DynamicConfigMapper;
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
    private DynamicConfigHistoryMapper historyMapper;

    @Autowired
    private MinioService minioService;

    @Override
    public DynamicConfig uploadConfig(MultipartFile file, String versionRange, String env, String remark) throws Exception {
        // 1. 上传文件到 MinIO
        String fileName = UUID.randomUUID() + ".json";
        String fileUrl = minioService.uploadFile(file, fileName, BucketType.DYNAMIC_CONFIG);

        // 2. 保存元数据到数据库
        DynamicConfig dynamicConfig = new DynamicConfig();
        dynamicConfig.setVersionRange(versionRange);
        dynamicConfig.setFileUrl(fileUrl);
        dynamicConfig.setEnv(env);
        dynamicConfig.setRemark(remark);
        
        dynamicConfigMapper.insert(dynamicConfig);
        
        // 3. 保存历史记录
        saveHistory(dynamicConfig, "CREATE", "system");
        
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
            // 保存删除前的历史记录（用于审计）
            saveHistory(config, "DELETE", "system");
            
            // 获取该配置的所有历史版本
            List<DynamicConfigHistory> histories = historyMapper.findByConfigId(id);
            
            // 删除当前配置的文件
            String currentFileName = config.getFileUrl().substring(config.getFileUrl().lastIndexOf("/") + 1);
            minioService.deleteFile(currentFileName, BucketType.DYNAMIC_CONFIG);
            
            // 删除所有历史版本的文件
            for (DynamicConfigHistory history : histories) {
                String historyFileName = history.getFileUrl().substring(history.getFileUrl().lastIndexOf("/") + 1);
                try {
                    minioService.deleteFile(historyFileName, BucketType.DYNAMIC_CONFIG);
                } catch (Exception e) {
                    // 记录日志但继续删除，避免某个文件删除失败影响整体操作
                    log.warn("删除历史文件失败: " + historyFileName, e);
                }
            }
            
            // 删除数据库记录
            dynamicConfigMapper.deleteById(id);
        }
    }

    @Override
    public DynamicConfig updateConfig(Long id, MultipartFile file, String versionRange, String env, String remark) throws Exception {
        DynamicConfig existingConfig = dynamicConfigMapper.findById(id);
        if (existingConfig == null) {
            throw new RuntimeException("配置不存在");
        }

        // 保存更新前的历史记录
        saveHistory(existingConfig, "UPDATE", "system");

        if (file != null && !file.isEmpty()) {
            // 上传新文件（不删除旧文件，保留历史版本）
            String newFileName = UUID.randomUUID() + ".json";
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
    public Object getConfigContent(Long id, String env) throws Exception {
        DynamicConfig config = dynamicConfigMapper.findByIdAndEnv(id, env);
        if (config == null) {
            throw new RuntimeException("配置不存在或环境不匹配");
        }

        return readJsonContentFromFile(config.getFileUrl());
    }

    /**
     * 保存历史记录
     */
    private void saveHistory(DynamicConfig config, String operationType, String operator) {
        DynamicConfigHistory history = new DynamicConfigHistory();
        history.setConfigId(config.getId());
        history.setVersionRange(config.getVersionRange());
        history.setFileUrl(config.getFileUrl());
        history.setEnv(config.getEnv());
        history.setRemark(config.getRemark());
        history.setOperationType(operationType);
        history.setOperator(operator);
        
        historyMapper.insert(history);
    }

    /**
     * 从文件URL读取并解析JSON内容
     */
    private Object readJsonContentFromFile(String fileUrl) throws Exception {
        // 从 URL 中提取文件名
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        
        try (InputStream inputStream = minioService.downloadFile(fileName, BucketType.DYNAMIC_CONFIG);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String fullContent = reader.lines().collect(Collectors.joining("\n"));
            String jsonContent = JsonUtil.minify(fullContent);
            return JsonUtil.toObject(jsonContent, Object.class);
        }
    }

    @Override
    public List<DynamicConfigHistory> getConfigHistory(Long configId) {
        return historyMapper.findByConfigId(configId);
    }

    @Override
    public List<DynamicConfigHistory> getAllHistory(String env, String versionRange) {
        return historyMapper.findHistoryWithFilters(env, versionRange);
    }

    @Override
    public DynamicConfig revertToHistory(Long configId, Long historyId, String operator) throws Exception {
        // 获取历史版本
        DynamicConfigHistory history = historyMapper.findById(historyId);
        if (history == null) {
            throw new RuntimeException("历史版本不存在");
        }
        
        // 获取当前配置
        DynamicConfig currentConfig = dynamicConfigMapper.findById(configId);
        if (currentConfig == null) {
            throw new RuntimeException("当前配置不存在");
        }
        
        // 保存当前配置的历史记录
        saveHistory(currentConfig, "UPDATE", operator);
        
        // 直接使用历史版本的文件URL（因为文件没有被删除）
        currentConfig.setVersionRange(history.getVersionRange());
        currentConfig.setFileUrl(history.getFileUrl());
        currentConfig.setEnv(history.getEnv());
        currentConfig.setRemark("回溯到历史版本ID: " + historyId + ". " + history.getRemark());
        
        dynamicConfigMapper.update(currentConfig);
        return currentConfig;
    }

    @Override
    public DynamicConfigHistory getHistoryById(Long historyId) {
        return historyMapper.findById(historyId);
    }

    @Override
    public Object getHistoryContent(Long historyId) throws Exception {
        DynamicConfigHistory history = historyMapper.findById(historyId);
        if (history == null) {
            throw new RuntimeException("历史版本不存在");
        }

        return readJsonContentFromFile(history.getFileUrl());
    }
}
