package com.p2p.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2p.domain.ChatMessage;
import com.p2p.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        Long roomId = extractRoomId(session);
        Long userId = extractUserId(session);

        sessions.put(sessionId, session);

        // 處理接收到的消息
        Mono<Void> input = session.receive()
            .doOnNext(message -> handleIncomingMessage(message, roomId, userId))
            .doOnError(error -> log.error("Error processing message", error))
            .then();

        // 訂閱房間消息
        Mono<Void> output = chatService.subscribeToRoom(roomId)
            .map(this::serialize)
            .map(session::textMessage)
            .as(session::send);

        // 清理工作
        return Mono.zip(input, output)
            .doFinally(signalType -> {
                sessions.remove(sessionId);
                log.info("WebSocket connection closed: {}", sessionId);
            })
            .then();
    }

    private void handleIncomingMessage(WebSocketMessage message, Long roomId, Long userId) {
        try {
            ChatMessage chatMessage = deserialize(message.getPayloadAsText());
            chatService.sendMessage(roomId, userId, chatMessage.getContent())
                .subscribe(
                    sent -> log.debug("Message sent: {}", sent.getId()),
                    error -> log.error("Error sending message", error)
                );
        } catch (Exception e) {
            log.error("Error processing incoming message", e);
        }
    }

    private Long extractRoomId(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        String roomId = path.substring(path.lastIndexOf('/') + 1);
        return Long.parseLong(roomId);
    }

    private Long extractUserId(WebSocketSession session) {
        return Long.parseLong(session.getHandshakeInfo()
            .getHeaders()
            .getFirst("X-User-ID"));
    }

    private String serialize(ChatMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            log.error("Error serializing message", e);
            throw new RuntimeException("Error serializing message", e);
        }
    }

    private ChatMessage deserialize(String json) {
        try {
            return objectMapper.readValue(json, ChatMessage.class);
        } catch (IOException e) {
            log.error("Error deserializing message", e);
            throw new RuntimeException("Error deserializing message", e);
        }
    }

    public void broadcastToRoom(Long roomId, ChatMessage message) {
        String serializedMessage = serialize(message);
        sessions.values().stream()
            .filter(session -> extractRoomId(session).equals(roomId))
            .forEach(session -> 
                session.send(Mono.just(session.textMessage(serializedMessage)))
                    .subscribe()
            );
    }
} 