package com.app.appplatform.dto;

import lombok.Data;

@Data
public class AppVersionCheckDto {
    /**
     * 是否有新版本
     */
    private boolean hasUpdate;
    
    /**
     * 最新版本号
     */
    private String latestVersion;
    
    /**
     * 最新构建号
     */
    private String latestBuildNumber;
    
    /**
     * 新版本特性描述
     */
    private String features;
    
    /**
     * 下载链接
     */
    private String downloadUrl;
    
    /**
     * 应用商店下载链接（根据设备品牌返回对应链接）
     */
    private String storeUrl;
    
    /**
     * 是否强制更新
     */
    private boolean forceUpdate = false;
    
    /**
     * 文件大小
     */
    private String fileSize;
}
