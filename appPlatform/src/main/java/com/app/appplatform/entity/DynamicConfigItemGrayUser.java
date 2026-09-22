package com.app.appplatform.entity;

import lombok.Data;

import java.util.Date;

/**
 * 动态配置项灰度名单实体类（白名单/黑名单，独立表存储）
 */
@Data
public class DynamicConfigItemGrayUser {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 配置项ID(dynamic_config_item.id)
     */
    private Long itemId;

    /**
     * 名单类型：WHITE(白名单)/BLACK(黑名单)
     */
    private String listType;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户名对应的昵称（仅用于列表展示，非持久化字段）
     */
    private String nickname;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
