package com.p2p.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2p.domain.Notification;
import com.p2p.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler implements WebSocketHandler {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        Long userId = extractUserId(session);
        sessions.put(sessionId, session);

        return session.receive()
            .doOnNext(message -> handleIncomingMessage(message, userId))
            .doFinally(signalType -> {
                sessions.remove(sessionId);
                log.info("WebSocket connection closed: {}", sessionId);
            })
            .then()
            .and(
                notificationService.subscribeToUserNotifications(userId)
                    .map(this::serialize)
                    .map(session::textMessage)
                    .as(session::send)
            );
    }

    private Long extractUserId(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        String userId = path.substring(path.lastIndexOf('/') + 1);
        return Long.parseLong(userId);
    }

    private void handleIncomingMessage(WebSocketMessage message, Long userId) {
        try {
            // 處理客戶端發送的消息（如果需要）
            log.debug("Received message from user {}: {}", userId, message.getPayloadAsText());
        } catch (Exception e) {
            log.error("Error handling incoming message", e);
        }
    }

    private String serialize(Notification notification) {
        try {
            return objectMapper.writeValueAsString(notification);
        } catch (Exception e) {
            log.error("Error serializing notification", e);
            throw new RuntimeException("Error serializing notification", e);
        }
    }

    public void sendNotificationToUser(Long userId, Notification notification) {
        String serializedNotification = serialize(notification);
        sessions.values().stream()
            .filter(session -> extractUserId(session).equals(userId))
            .forEach(session -> 
                session.send(Mono.just(session.textMessage(serializedNotification)))
                    .subscribe()
            );
    }
} 