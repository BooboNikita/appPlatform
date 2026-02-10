package com.app.appplatform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 绩效评估配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReview {
    private Long id;
    private String deptId;
    private String name;
    private String coverImage;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private String deadline;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
