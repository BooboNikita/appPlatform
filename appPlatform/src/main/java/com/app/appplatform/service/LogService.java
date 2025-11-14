package com.app.appplatform.service;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.entity.LogInfo;

public interface LogService {
    void save(LogInfo logInfo);
    
    LogInfo getLogById(Integer id);
    
    PageResult<LogInfo> getLogList(int pageNum, int pageSize, String appName, String username, String startDate, String endDate);
    
    void deleteLog(Integer id);
}
