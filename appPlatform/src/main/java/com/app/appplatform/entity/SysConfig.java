package com.app.appplatform.entity;

import lombok.Data;

import java.util.Date;

/**
 * 系统配置实体类
 */
@Data
public class SysConfig {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

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
}