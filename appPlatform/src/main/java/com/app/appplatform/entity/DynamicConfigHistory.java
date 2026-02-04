package com.app.appplatform.entity;

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
     * 原配置ID
     */
    private Long configId;

    /**
     * 版本范围或具体版本，如 1.0.0-2.0.0 或 1.5.0
     */
    private String versionRange;

    /**
     * MinIO中的文件保存地址
     */
    private String fileUrl;

    /**
     * 环境类型：prod(生产), test(测试)
     */
    private String env;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作类型：CREATE(创建), UPDATE(更新), DELETE(删除)
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
