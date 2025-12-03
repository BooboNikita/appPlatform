package com.app.appplatform.websocket;

import com.app.appplatform.service.JwtUserDetailsService;
import com.app.appplatform.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.handler.codec.http.HttpHeaderValidationUtil;

@Slf4j
@Component
public class NativeWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 新连接建立时调用
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String token = extractTokenFromUri(session.getUri());

        if (token == null || !jwtTokenUtil.validateToken(token)) {
            log.warn("无效的 token，关闭连接");
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("无效的 token"));
            } catch (IOException e) {
                System.err.println("关闭WebSocket连接时出错: " + e.getMessage());
                // 可以选择记录更详细的日志
            }
            return;
        }

        String sessionId = session.getId();
        sessions.put(sessionId, session);
        System.out.println("新连接建立, sessionId: " + sessionId);
        
        // 发送欢迎消息
        sendMessage(session, createMessage("system", "连接成功", null));
    }

    private String extractTokenFromUri(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }

        // 解析查询参数
        String query = uri.getQuery();
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    // 处理接收到的消息
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);
        
        try {
            // 解析JSON消息
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            String type = (String) messageMap.get("type");
            Object content = messageMap.get("content");
            
            // 根据消息类型处理
            switch (type) {
                case "echo":
                    // 回显消息
                    sendMessage(session, createMessage("echo", content, null));
                    break;
                case "broadcast":
                    // 广播消息给所有连接
                    broadcast(createMessage("broadcast", content, session.getId()));
                    break;
                default:
                    sendMessage(session, createMessage("error", "未知的消息类型: " + type, null));
            }
        } catch (Exception e) {
            sendMessage(session, createMessage("error", "消息格式错误: " + e.getMessage(), null));
        }
    }

    // 处理传输错误
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("WebSocket传输错误: " + exception.getMessage());
    }

    // 连接关闭时调用
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        System.out.println("连接关闭, sessionId: " + sessionId + ", 状态: " + status);
    }

    // 发送消息给指定会话
    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
        }
    }

    // 广播消息给所有连接
    public void broadcast(String message) {
        sessions.forEach((id, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("广播消息失败: " + e.getMessage());
                }
            }
        });
    }

    // 创建JSON格式的消息
    private String createMessage(String type, Object content, String from) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "content", content != null ? content : "",
                    "timestamp", System.currentTimeMillis(),
                    "from", from != null ? from : "server"
            ));
        } catch (Exception e) {
            return "{\"type\":\"error\",\"content\":\"创建消息失败\"}";
        }
    }

    // 发送事件消息
    public void sendEvent(String eventType, Object data) {
        String message = createMessage("event", Map.of(
                "eventType", eventType,
                "data", data
        ), null);
        broadcast(message);
    }
}
