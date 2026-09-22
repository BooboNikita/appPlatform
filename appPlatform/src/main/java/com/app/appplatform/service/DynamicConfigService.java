package com.app.appplatform.service;

import com.app.appplatform.entity.DynamicConfig;
import com.app.appplatform.entity.DynamicConfigHistory;
import com.app.appplatform.entity.DynamicConfigItem;
import com.app.appplatform.entity.DynamicConfigItemGrayUser;
import com.app.appplatform.entity.DynamicConfigItemHistory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DynamicConfigService {
    /**
     * 上传并保存动态配置（整包 JSON，服务端拆解为配置项入库）
     */
    DynamicConfig uploadConfig(MultipartFile file, String versionRange, String env, String remark) throws Exception;

    /**
     * 根据 ID 获取配置
     */
    DynamicConfig getConfigById(Long id);

    /**
     * 获取所有配置
     */
    List<DynamicConfig> getAllConfigs(String env);

    /**
     * 根据版本和环境获取适用的最新配置
     */
    DynamicConfig getLatestConfigByVersion(String version, String env);

    /**
     * 删除配置
     */
    void deleteConfig(Long id) throws Exception;

    /**
     * 更新配置
     */
    DynamicConfig updateConfig(Long id, MultipartFile file, String versionRange, String env, String remark) throws Exception;

    /**
     * 获取配置文件的内容（管理端全量，不做灰度过滤）
     */
    Object getConfigContent(Long id, String env) throws Exception;

    // ==================== 配置项管理 ====================

    /**
     * 扁平配置项列表（支持按环境、版本范围、域、关键字筛选）
     */
    List<DynamicConfigItem> getItems(String env, String versionRange, String domain, String keyword);

    /**
     * 更新单个配置项（值 / 启用状态 / 灰度开关 / 灰度百分比 / 备注；不含名单）
     */
    DynamicConfigItem updateItem(Long itemId, DynamicConfigItem req, String operator) throws Exception;

    /**
     * 单独新增配置项（指定所属配置，同配置下域+配置项不可重复），写一条 CREATE 历史
     */
    DynamicConfigItem addItem(DynamicConfigItem req, String operator) throws Exception;

    /**
     * 删除单个配置项（级联删除其灰度名单，并写一条 DELETE 历史）
     */
    void deleteItem(Long itemId, String operator) throws Exception;

    // ==================== 灰度名单 ====================

    /**
     * 查询某配置项指定类型的名单
     */
    List<DynamicConfigItemGrayUser> getGrayUsers(Long itemId, String listType, String keyword);

    /**
     * 批量新增名单（幂等，返回实际新增条数）；成功后写一条该配置项 UPDATE 历史
     */
    int addGrayUsers(Long itemId, String listType, List<String> usernames, String operator) throws Exception;

    /**
     * 删除单条名单；成功后写一条该配置项 UPDATE 历史
     */
    void deleteGrayUser(Long grayUserId, String operator) throws Exception;

    /**
     * 清空某配置项指定类型的名单；成功后写一条该配置项 UPDATE 历史
     */
    void clearGrayUsers(Long itemId, String listType, String operator) throws Exception;

    /**
     * 反查某用户名命中的配置项 ID 列表
     */
    List<Long> findItemIdsByUsername(String username);

    // ==================== 客户端下发 ====================

    /**
     * 按版本+环境选中配置，按 username 灰度过滤后拼装下发 JSON
     */
    Object matchConfigContent(String version, String env, String username) throws Exception;

    // ==================== 历史版本相关方法 ====================

    /**
     * 获取指定配置的历史版本列表（整包快照）
     */
    List<DynamicConfigHistory> getConfigHistory(Long configId);

    /**
     * 获取所有历史版本（支持筛选）
     */
    List<DynamicConfigHistory> getAllHistory(String env, String versionRange);

    /**
     * 回溯到指定历史版本（整包，覆盖该配置下全部配置项与名单）
     */
    DynamicConfig revertToHistory(Long configId, Long historyId, String operator) throws Exception;

    /**
     * 获取历史版本详情
     */
    DynamicConfigHistory getHistoryById(Long historyId);

    /**
     * 获取历史版本的配置内容（整包快照解析）
     */
    Object getHistoryContent(Long historyId) throws Exception;

    // ==================== 配置项级历史与回溯 ====================

    /**
     * 某配置项的变更历史（按业务键 config_id + domain + item_key 查询）
     */
    List<DynamicConfigItemHistory> getItemHistory(Long itemId);

    /**
     * 单条配置项历史详情
     */
    DynamicConfigItemHistory getItemHistoryById(Long historyId);

    /**
     * 单项回溯：把指定历史的状态写回该配置项（含值/启用/灰度/百分比/名单），仅影响该配置项
     */
    DynamicConfigItem revertItemToHistory(Long itemId, Long historyId, String operator) throws Exception;
}
