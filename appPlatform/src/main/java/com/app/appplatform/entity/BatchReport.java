package com.app.appplatform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 批量上报记录实体类
 * 对应batch_reports表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchReport {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failedCount;
    
    /**
     * 失败的崩溃ID列表(JSON格式)
     */
    private String failedCrashIds;
    
    /**
     * 上报时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime reportTimestamp;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "BatchReport{" +
                "id=" + id +
                ", appId='" + appId + '\'' +
                ", totalCount=" + totalCount +
                ", successCount=" + successCount +
                ", failedCount=" + failedCount +
                ", reportTimestamp=" + reportTimestamp +
                ", createdAt=" + createdAt +
                '}';
    }
}
