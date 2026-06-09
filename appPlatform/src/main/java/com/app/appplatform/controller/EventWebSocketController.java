package com.app.appplatform.controller;

import com.app.appplatform.entity.AppEvent;
import com.app.appplatform.websocket.NativeWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventWebSocketController {

    private final NativeWebSocketHandler webSocketHandler;

    @Autowired
    public EventWebSocketController(NativeWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    // 当新事件保存时调用此方法
    public void sendEventUpdate(AppEvent event) {
        // 使用NativeWebSocketHandler发送事件
        webSocketHandler.sendEvent("app_event", event);
    }
}
