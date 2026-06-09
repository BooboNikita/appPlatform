package com.app.app.service.impl;

import com.app.app.entity.DynamicConfig;
import com.app.app.entity.DynamicConfigHistory;
import com.app.app.mapper.DynamicConfigHistoryMapper;
import com.app.app.mapper.DynamicConfigMapper;
import com.app.app.service.DynamicConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class DynamicConfigServiceImpl implements DynamicConfigService {

    private final DynamicConfigMapper dynamicConfigMapper;
    private final DynamicConfigHistoryMapper historyMapper;

    @Autowired
    public DynamicConfigServiceImpl(DynamicConfigMapper dynamicConfigMapper, DynamicConfigHistoryMapper historyMapper) {
        this.dynamicConfigMapper = dynamicConfigMapper;
        this.historyMapper = historyMapper;
    }

    @Override
    public DynamicConfig uploadConfig(MultipartFile file, String versionRange, String env, String remark) throws Exception {
        // TODO: 上传文件到MinIO

        DynamicConfig config = new DynamicConfig();
        config.setVersionRange(versionRange);
        config.setEnv(env);
        config.setRemark(remark);
        config.setCreateTime(new Date());
        config.setUpdateTime(new Date());
        // config.setFileUrl(fileUrl);

        dynamicConfigMapper.insert(config);

        // 记录历史
        saveHistory(config, "create", "system");

        return config;
    }

    @Override
    public DynamicConfig getConfigById(Long id) {
        return dynamicConfigMapper.selectById(id);
    }

    @Override
    public List<DynamicConfig> getAllConfigs(String env) {
        if (env != null && !env.isEmpty()) {
            return dynamicConfigMapper.selectByEnv(env);
        }
        return dynamicConfigMapper.selectAll();
    }

    @Override
    public DynamicConfig getLatestConfigByVersion(String version, String env) {
        return dynamicConfigMapper.selectLatestByVersionAndEnv(version, env);
    }

    @Override
    public void deleteConfig(Long id) throws Exception {
        DynamicConfig config = dynamicConfigMapper.selectById(id);
        if (config != null) {
            // 记录历史
            saveHistory(config, "delete", "system");
            dynamicConfigMapper.deleteById(id);
            // TODO: 删除MinIO文件
        }
    }

    @Override
    public DynamicConfig updateConfig(Long id, MultipartFile file, String versionRange, String env, String remark) throws Exception {
        DynamicConfig config = dynamicConfigMapper.selectById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        // TODO: 如果上传了新文件，更新MinIO文件

        if (versionRange != null) {
            config.setVersionRange(versionRange);
        }
        if (env != null) {
            config.setEnv(env);
        }
        if (remark != null) {
            config.setRemark(remark);
        }
        config.setUpdateTime(new Date());

        dynamicConfigMapper.update(config);

        // 记录历史
        saveHistory(config, "update", "system");

        return config;
    }

    @Override
    public Object getConfigContent(Long id, String env) throws Exception {
        DynamicConfig config = dynamicConfigMapper.selectById(id);
        if (config == null) {
            return null;
        }
        // TODO: 从MinIO读取文件内容
        return null;
    }

    @Override
    public List<DynamicConfigHistory> getConfigHistory(Long configId) {
        return historyMapper.selectByConfigId(configId);
    }

    @Override
    public List<DynamicConfigHistory> getAllHistory(String env, String versionRange) {
        return historyMapper.selectByCondition(env, versionRange);
    }

    @Override
    public DynamicConfig revertToHistory(Long configId, Long historyId, String operator) throws Exception {
        DynamicConfigHistory history = historyMapper.selectById(historyId);
        if (history == null) {
            throw new RuntimeException("历史版本不存在");
        }

        DynamicConfig config = dynamicConfigMapper.selectById(configId);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        // 恢复到历史版本
        config.setVersionRange(history.getVersionRange());
        config.setFileUrl(history.getFileUrl());
        config.setEnv(history.getEnv());
        config.setRemark(history.getRemark());
        config.setUpdateTime(new Date());

        dynamicConfigMapper.update(config);

        // 记录历史
        saveHistory(config, "revert", operator);

        return config;
    }

    @Override
    public DynamicConfigHistory getHistoryById(Long historyId) {
        return historyMapper.selectById(historyId);
    }

    @Override
    public Object getHistoryContent(Long historyId) throws Exception {
        DynamicConfigHistory history = historyMapper.selectById(historyId);
        if (history == null) {
            return null;
        }
        // TODO: 从MinIO读取历史文件内容
        return null;
    }

    private void saveHistory(DynamicConfig config, String operationType, String operator) {
        DynamicConfigHistory history = new DynamicConfigHistory();
        history.setConfigId(config.getId());
        history.setVersionRange(config.getVersionRange());
        history.setFileUrl(config.getFileUrl());
        history.setEnv(config.getEnv());
        history.setRemark(config.getRemark());
        history.setOperationType(operationType);
        history.setOperator(operator);
        history.setCreateTime(new Date());
        historyMapper.insert(history);
    }
}
