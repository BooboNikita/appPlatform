package com.app.appplatform.entity;

import lombok.Data;
import java.util.Date;

@Data
public class LogInfo {
    private Integer id;
    private String username;
    private String nickname;
    private Date uploadTime;
    private String path;  // 多个文件路径用逗号分隔
    private String appName;
    private String version;
    private String imageUrls;
    private String problem;
}
