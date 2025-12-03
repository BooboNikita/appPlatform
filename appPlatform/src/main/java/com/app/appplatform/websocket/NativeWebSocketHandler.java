package com.app.appplatform.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NativeWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 新连接建立时调用
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        System.out.println("新连接建立, sessionId: " + sessionId);
        
        // 发送欢迎消息
        sendMessage(session, createMessage("system", "连接成功", null));
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
