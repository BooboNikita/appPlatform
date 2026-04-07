package com.app.appplatform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Flutter Crash SDK API Protocol - Base Request Wrapper
 * 2024-04-07 API Protocol
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseCrashRequest<T> {
    
    private T data;
}
