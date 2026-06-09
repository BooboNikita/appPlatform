package com.app.app.entity;

import lombok.Data;

import java.util.Date;

/**
 * 动态配置历史版本实体类
 */
@Data
public class DynamicConfigHistory {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联的动态配置ID
     */
    private Long configId;

    /**
     * 版本范围
     */
    private String versionRange;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 环境类型
     */
    private String env;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作类型：create, update, delete, revert
     */
    private String operationType;

    /**
     * 创建时间
     */
    private Date createTime;
}
