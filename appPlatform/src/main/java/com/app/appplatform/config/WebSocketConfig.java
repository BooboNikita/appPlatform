package com.app.appplatform.config;

import com.app.appplatform.websocket.NativeWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NativeWebSocketHandler nativeWebSocketHandler;

    public WebSocketConfig(NativeWebSocketHandler nativeWebSocketHandler) {
        this.nativeWebSocketHandler = nativeWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(nativeWebSocketHandler, "/ws")
                .setAllowedOriginPatterns("*");
    }
}
