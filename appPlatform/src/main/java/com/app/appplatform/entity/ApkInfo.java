package com.app.appplatform.entity;

import lombok.Data;

@Data
public class ApkInfo {
    private String version;
    private String pkgName;
    private String fileName;
    private Boolean mtime;
}
