package com.app.appplatform.service.impl;

import com.app.appplatform.common.PageResult;
import com.app.appplatform.controller.EventWebSocketController;
import com.app.appplatform.entity.AppEvent;
import com.app.appplatform.mapper.AppEventMapper;
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
import java.util.List;

@Service
public class AppEventServiceImpl implements AppEventService {

    @Autowired
    private AppEventMapper appEventMapper;

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
        if (appEvent.getEvent().getRecvTime() == null) {
            appEvent.getEvent().setRecvTime(LocalDateTime.now());
        }
        appEventMapper.insert(appEvent);
        // 发送WebSocket通知
        webSocketController.sendEventUpdate(appEvent);
    }

    @Override
    @Transactional
    public void batchSaveEvents(List<AppEvent> events) {
        events.forEach(event -> {
            if (event.getEvent().getRecvTime() == null) {
                event.getEvent().setRecvTime(LocalDateTime.now());
            }
            appEventMapper.insert(event);
        });
    }

    @Override
    public PageResult<AppEvent> getRecentEvents(int pageNum, int pageSize) {
        // 使用PageHelper进行分页
        PageHelper.startPage(pageNum, pageSize);
        List<AppEvent> events = appEventMapper.selectRecentEvents();
        PageInfo<AppEvent> pageInfo = new PageInfo<>(events);
        
        return new PageResult<>(
            pageInfo.getList(),
            pageInfo.getTotal(),
            pageInfo.getPageNum(),
            pageInfo.getPageSize(),
            pageInfo.getPages()
        );
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
