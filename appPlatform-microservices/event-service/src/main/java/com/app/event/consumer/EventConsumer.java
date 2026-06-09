package com.app.event.consumer;

import com.app.event.entity.AppEvent;
import com.app.event.service.AppEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class EventConsumer {

    @Autowired
    private AppEventService appEventService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue.event}")
    public void receiveEvent(@Payload String message) {
        try {
            AppEvent event = objectMapper.readValue(message, AppEvent.class);
            log.info("收到事件: {}", event.getEventInfo().getEventId());
            appEventService.saveEvent(event);
        } catch (IOException e) {
            log.error("处理消息失败: {}", message, e);
        }
    }
}
