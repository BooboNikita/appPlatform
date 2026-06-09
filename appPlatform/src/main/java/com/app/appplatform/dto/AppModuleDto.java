package com.app.appplatform.dto;

import lombok.Data;

@Data
public class AppModuleDto {
    private String title;
    private String icon;
    private String url;
    private String route;
    private String color;
    private Integer port;
}
