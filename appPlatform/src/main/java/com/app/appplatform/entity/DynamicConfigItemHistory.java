package com.app.appplatform.entity;

import lombok.Data;

import java.util.Date;

/**
 * 动态配置项历史实体类（记录单项每次变更后的状态，含名单快照）
 * 以业务键 (config_id, domain, item_key) 关联，保证整包回溯（删旧插新）后历史仍连续。
 */
@Data
public class DynamicConfigItemHistory {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 所属配置ID(dynamic_config.id)
     */
    private Long configId;

    /**
     * 所属域
     */
    private String domain;

    /**
     * 配置项 key
     */
    private String itemKey;

    /**
     * 版本范围(冗余，便于展示)
     */
    private String versionRange;

    /**
     * 环境(冗余，便于展示)
     */
    private String env;

    /**
     * 本次变更后的值(JSON 序列化文本)
     */
    private String itemValue;

    /**
     * 值类型：BOOLEAN/NUMBER/STRING/JSON
     */
    private String valueType;

    /**
     * 本次变更后是否启用
     */
    private Integer enabled;

    /**
     * 本次变更后是否开启灰度
     */
    private Integer grayEnabled;

    /**
     * 本次变更后的灰度百分比
     */
    private Integer grayPercent;

    /**
     * 名单快照 JSON 数组，如 [{"listType":"WHITE","username":"u1"}]
     */
    private String grayUsersJson;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作类型：CREATE/UPDATE/DELETE/REVERT
     */
    private String operationType;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    private Date createTime;
}
