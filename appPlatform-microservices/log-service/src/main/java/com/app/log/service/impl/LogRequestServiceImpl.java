package com.app.log.service.impl;

import com.app.log.entity.LogRequest;
import com.app.log.mapper.LogRequestMapper;
import com.app.log.service.LogRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 日志请求记录服务实现类
 * 使用Redis存储超时信息，提高性能和可靠性
 */
@Service
public class LogRequestServiceImpl implements LogRequestService {

    private final LogRequestMapper logRequestMapper;

    private final StringRedisTemplate redisTemplate;

    @Value("${log.request.timeout-minutes:120}")
    private Integer defaultTimeoutMinutes;

    // Redis key前缀
    @Value("${log.request.redis-key:log:request:}")
    private String logRequestKeyPrefix;

    @Autowired
    public LogRequestServiceImpl(LogRequestMapper logRequestMapper, StringRedisTemplate redisTemplate) {
        this.logRequestMapper = logRequestMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成Redis key
     */
    private String getRedisKey(Integer requestId) {
        return logRequestKeyPrefix + requestId;
    }

    @Override
    @Transactional
    public LogRequest createLogRequest(String username, Integer timeoutMinutes) {
        // 使用传入的超时时间，如果为空则使用默认配置
        int timeout = (timeoutMinutes != null && timeoutMinutes > 0) ? timeoutMinutes : defaultTimeoutMinutes;

        LogRequest logRequest = new LogRequest();
        logRequest.setUsername(username);
        logRequest.setRequestTime(new Date());
        logRequest.setStatus(0);  // 待上传状态

        // 计算过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, timeout);
        logRequest.setExpireTime(calendar.getTime());

        // 先保存到数据库
        logRequestMapper.insert(logRequest);

        // 将超时信息存入Redis，设置过期时间
        String redisKey = getRedisKey(logRequest.getId());
        // 存储状态为待上传(0)，并设置过期时间
        redisTemplate.opsForValue().set(redisKey, "0", timeout, TimeUnit.MINUTES);

        return logRequest;
    }

    @Override
    public List<LogRequest> getPendingLogRequest(String username) {
        // 查询所有待上传的请求
        List<LogRequest> pendingRequests = logRequestMapper.findActiveByUsername(username);

        if (pendingRequests.isEmpty()) {
            return pendingRequests;
        }

        List<LogRequest> resultRequests = new ArrayList<>();
        // 遍历检查每个请求是否过期
        for (LogRequest logRequest : pendingRequests) {
            String redisKey = getRedisKey(logRequest.getId());
            String status = redisTemplate.opsForValue().get(redisKey);

            // 如果Redis中没有该key，说明已过期
            if (status == null && logRequest.getStatus() == 0) {
                // 更新数据库状态为已过期
                logRequestMapper.updateStatus(logRequest.getId(), 2);
            } else {
                // 未过期的请求加入结果列表
                resultRequests.add(logRequest);
            }
        }

        return resultRequests;
    }

    @Override
    public List<LogRequest> getLogRequestList(String username, Integer status, String startDate, String endDate) {
        return logRequestMapper.findByCondition(username, status, startDate, endDate);
    }

    @Override
    @Transactional
    public void deleteLogRequest(Integer id) {
        // 删除Redis中的key
        String redisKey = getRedisKey(id);
        redisTemplate.delete(redisKey);

        // 删除数据库记录
        logRequestMapper.delete(id);
    }

    @Override
    @Transactional
    public void markAsUploadedByUsername(String username) {
        // 查询该用户所有待上传的日志请求
        List<LogRequest> pendingRequests = getPendingLogRequest(username);

        // 删除所有待上传请求的Redis key并更新状态
        for (LogRequest pendingRequest : pendingRequests) {
            String redisKey = getRedisKey(pendingRequest.getId());
            redisTemplate.delete(redisKey);
            // 更新具体请求状态为已上传
            logRequestMapper.updateStatus(pendingRequest.getId(), 1);
        }
    }
}
