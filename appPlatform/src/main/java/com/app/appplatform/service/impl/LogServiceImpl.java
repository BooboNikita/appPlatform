package com.app.appplatform.service.impl;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.entity.LogInfo;
import com.app.appplatform.mapper.primary.LogMapper;
import com.app.appplatform.service.LogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;

    @Autowired
    public LogServiceImpl(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public void save(LogInfo logInfo) {
        if (logInfo.getId() == null) {
            logMapper.insert(logInfo);
        } else {
            logMapper.update(logInfo);
        }
    }

    @Override
    public LogInfo getLogById(Integer id) {
        return logMapper.findById(id);
    }

    @Override
    public PageResult<LogInfo> getLogList(int pageNum, int pageSize, String appName, String username, String startDate, String endDate) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 获取用户角色
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
                
        // 如果不是管理员，返回空结果
        if (!isAdmin) {
            return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize, 0);
        }
        
        // 处理日期格式，确保是 YYYY-MM-DD 格式
        if (startDate != null && startDate.length() > 10) {
            startDate = startDate.substring(0, 10);
        }
        if (endDate != null && endDate.length() > 10) {
            // 结束日期设置为当天的23:59:59
            endDate = endDate.substring(0, 10) + " 23:59:59";
        }
        
        Page<LogInfo> page = PageHelper.startPage(pageNum, pageSize);
        List<LogInfo> logs = logMapper.findByCondition(appName, username, startDate, endDate);
        return new PageResult<>(logs, page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages());
    }

    @Override
    @Transactional
    public void deleteLog(Integer id) {
        logMapper.delete(id);
    }
}
