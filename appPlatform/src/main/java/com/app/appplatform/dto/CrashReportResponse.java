package com.app.appplatform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrashReportResponse {
    
    private boolean success;
    
    private String message;
    
    private String crashId;
}
