package com.app.appplatform.service.impl;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.controller.EventWebSocketController;
import com.app.appplatform.entity.AppEvent;
import com.app.appplatform.mapper.primary.AppEventMapper;
import com.app.appplatform.mapper.secondary.UserSecondaryMapper;
import com.app.appplatform.service.AppEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppEventServiceImpl implements AppEventService {

    @Autowired
    private AppEventMapper appEventMapper;

    @Autowired
    private UserSecondaryMapper userSecondaryMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${rabbitmq.exchange.event}")
    private String eventExchange;

    @Value("${rabbitmq.routing-key.event}")
    private String eventRoutingKey;

    @Autowired
    private EventWebSocketController webSocketController;

    @Override
    @Transactional
    public void saveEvent(AppEvent appEvent) {
        if (appEvent.getEventInfo().getRecvTime() == null) {
            appEvent.getEventInfo().setRecvTime(LocalDateTime.now(ZoneOffset.systemDefault()));
        }
        appEventMapper.insert(appEvent);
        // 发送WebSocket通知
        webSocketController.sendEventUpdate(appEvent);
    }

    @Override
    @Transactional
    public void batchSaveEvents(List<AppEvent> events) {
        events.forEach(event -> {
            if (event.getEventInfo().getRecvTime() == null) {
                event.getEventInfo().setRecvTime(LocalDateTime.now(ZoneOffset.UTC));
            }
            appEventMapper.insert(event);
        });
    }

    @Override
    public PageResult<AppEvent> getRecentEvents(int pageNum, int pageSize, 
            String userId, String userName, String nickname, String eventId, String pageUrl, String eventType,
            String filterKey, String filterValue) {
        
        // 如果提供了 nickname，先转换为 username
        String actualUserName = userName;
        if (nickname != null && !nickname.trim().isEmpty() && (userName == null || userName.trim().isEmpty())) {
            try {
                String usernameFromNickname = userSecondaryMapper.findUsernameByNickname(nickname.trim());
                if (usernameFromNickname != null && !usernameFromNickname.trim().isEmpty()) {
                    // 只有在没有提供 userName 时才使用 nickname 转换的结果
                    actualUserName = usernameFromNickname;
                }
            } catch (Exception e) {
                System.err.println("根据昵称查找用户名失败: " + e.getMessage());
                // 如果查找失败，继续使用原始的 userName
            }
        }
        
        // 使用PageHelper进行分页
        PageHelper.startPage(pageNum, pageSize);
        List<AppEvent> events;
        
        if (userId != null || actualUserName != null || eventId != null || pageUrl != null || eventType != null || 
            (filterKey != null && filterValue != null)) {
            // 使用带过滤条件的查询
            events = appEventMapper.selectRecentEventsWithFilters(
                userId, actualUserName, eventId, pageUrl, eventType, filterKey, filterValue);
        } else {
            // 使用普通查询
            events = appEventMapper.selectRecentEvents();
        }
        
        // 为事件列表批量查询并添加用户昵称
        enrichEventsWithUserNicknames(events);
        
        PageInfo<AppEvent> pageInfo = new PageInfo<>(events);
        
        return new PageResult<>(
            pageInfo.getList(),
            pageInfo.getTotal(),
            pageInfo.getPageNum(),
            pageInfo.getPageSize(),
            pageInfo.getPages()
        );
    }

    /**
     * 批量查询用户昵称并添加到事件列表中
     * 优化性能：使用批量查询避免 N+1 问题
     * 
     * @param events 事件列表
     */
    private void enrichEventsWithUserNicknames(List<AppEvent> events) {
        // 收集所有需要查询的用户名（去重）
        Map<String, List<AppEvent>> userToEventsMap = new HashMap<>();
        events.forEach(event -> {
            if (event.getUserName() != null && !event.getUserName().trim().isEmpty()) {
                userToEventsMap.computeIfAbsent(event.getUserName(), k -> new ArrayList<>()).add(event);
            }
        });
        
        if (userToEventsMap.isEmpty()) {
            return;
        }
        
        try {
            // 批量查询用户昵称
            List<String> usernames = new ArrayList<>(userToEventsMap.keySet());
            List<Map<String, Object>> userInfos = userSecondaryMapper.findNicknamesByUsernames(usernames);
            
            // 构建用户名到昵称的映射
            Map<String, String> usernameToNicknameMap = new HashMap<>();
            userInfos.forEach(userInfo -> {
                String username = (String) userInfo.get("username");
                String nickname = (String) userInfo.get("nickname");
                if (username != null && nickname != null && !nickname.trim().isEmpty()) {
                    usernameToNicknameMap.put(username, nickname);
                }
            });
            
            // 为事件添加昵称
            userToEventsMap.forEach((username, eventList) -> {
                String nickname = usernameToNicknameMap.get(username);
                if (nickname != null) {
                    eventList.forEach(event -> addNicknameToEvent(event, nickname));
                }
            });
            
        } catch (Exception e) {
            System.err.println("批量查询用户昵称失败: " + e.getMessage());
            // 降级到单个查询
            userToEventsMap.keySet().forEach(username -> {
                try {
                    String nickname = userSecondaryMapper.findNicknameByUsername(username);
                    if (nickname != null && !nickname.trim().isEmpty()) {
                        userToEventsMap.get(username).forEach(event -> addNicknameToEvent(event, nickname));
                    }
                } catch (Exception ex) {
                    System.err.println("单个查询用户昵称失败: " + ex.getMessage());
                }
            });
        }
    }

    /**
     * 将昵称直接设置到事件对象的 nickname 字段中
     * 
     * @param event 事件对象
     * @param nickname 用户昵称
     */
    private void addNicknameToEvent(AppEvent event, String nickname) {
        if (event != null && nickname != null && !nickname.trim().isEmpty()) {
            event.setNickname(nickname);
        }
    }

    /**
     * 为单个事件对象添加用户昵称信息
     * 从副数据库 xxg_idaas 的 ei_user 表中查询
     * 
     * @param event 事件对象
     */
    private void enrichEventWithUserNickname(AppEvent event) {
        if (event.getUserName() != null && !event.getUserName().trim().isEmpty()) {
            try {
                // 从副数据库查询用户昵称
                String nickname = userSecondaryMapper.findNicknameByUsername(event.getUserName());
                
                // 将昵称直接设置到事件对象中
                if (nickname != null && !nickname.trim().isEmpty()) {
                    addNicknameToEvent(event, nickname);
                }
            } catch (Exception e) {
                // 记录错误但不影响主要业务流程
                System.err.println("查询用户昵称失败: " + e.getMessage());
            }
        }
    }

    @Override
    public void sendToQueue(AppEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(eventExchange, eventRoutingKey, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
