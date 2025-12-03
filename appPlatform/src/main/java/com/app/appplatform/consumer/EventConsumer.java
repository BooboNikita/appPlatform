package com.app.appplatform.consumer;

import com.app.appplatform.entity.AppEvent;
import com.app.appplatform.service.AppEventService;
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
            log.info("Received event: {}", event.getEvent().getEventId());
            appEventService.saveEvent(event);
        } catch (IOException e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
