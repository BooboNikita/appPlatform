package com.app.appplatform.entity;

import lombok.Data;

import java.util.Date;

/**
 * 动态配置元数据实体类
 */
@Data
public class DynamicConfig {
    /**
     * 主键ID
     */
    private Long id;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
