package com.app.appplatform.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AppInfoDto {
    private Integer id;
    private String appName;     // 应用名称
    private String packageName; // 包名
    private String version;     // 版本号
    private String buildNumber; // 构建号
    private String features;    // 新特性
    private Boolean isBeta;     // 是否为beta版本
    private Date createTime;    // 创建时间
    private Integer downloadTimes; // 下载次数
    private String size;        // 文件大小
}
