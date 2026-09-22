package com.app.appplatform.entity;

import lombok.Data;

import java.util.Date;

/**
 * 动态配置项实体类（动态配置按 域+配置项key 拆解后的最小下发单元）
 */
@Data
public class DynamicConfigItem {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 所属配置ID(dynamic_config.id)
     */
    private Long configId;

    /**
     * 版本范围，如 1.0.0-2.0.0 / 1.5.0 / *（从所属配置冗余）
     */
    private String versionRange;

    /**
     * 环境：prod(生产), test(测试)（从所属配置冗余）
     */
    private String env;

    /**
     * 所属域，如 homepage/detail/global
     */
    private String domain;

    /**
     * 配置项 key，如 ai_entry
     */
    private String itemKey;

    /**
     * 配置项值（JSON 序列化文本，保留原始类型）
     */
    private String itemValue;

    /**
     * 值类型：BOOLEAN/NUMBER/STRING/JSON
     */
    private String valueType;

    /**
     * 是否启用：1启用(下发)，0停用(不下发)
     */
    private Integer enabled;

    /**
     * 是否开启灰度：1开启，0关闭(全量下发)
     */
    private Integer grayEnabled;

    /**
     * 灰度百分比 0-100
     */
    private Integer grayPercent;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    // ========== 非表字段（列表页展示用） ==========

    /**
     * 白名单用户数（列表查询聚合）
     */
    private Integer whiteCount;

    /**
     * 黑名单用户数（列表查询聚合）
     */
    private Integer blackCount;
}
