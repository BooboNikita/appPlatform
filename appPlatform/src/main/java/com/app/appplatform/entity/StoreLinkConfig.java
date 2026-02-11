package com.app.appplatform.entity;

import lombok.Data;

import java.util.Date;

/**
 * 应用商店链接配置实体类
 */
@Data
public class StoreLinkConfig {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 设备品牌（如：xiaomi、huawei、honor、oppo等）
     */
    private String deviceBrand;

    /**
     * 应用商店链接模板（如：market://details?id={packageName}）
     */
    private String linkTemplate;

    /**
     * 是否启用（1=启用，0=禁用）
     */
    private Integer enabled;

    /**
     * 排序权重
     */
    private Integer sortOrder;

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
