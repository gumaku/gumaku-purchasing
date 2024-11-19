package com.p2p.controller;

import com.p2p.domain.ChatMessage;
import com.p2p.domain.ChatRoom;
import com.p2p.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public Mono<ResponseEntity<ChatRoom>> createChatRoom(
            @RequestParam String name,
            @RequestParam(required = false) Long orderId) {
        return chatService.createChatRoom(name, orderId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @GetMapping("/rooms/{roomId}")
    public Mono<ResponseEntity<ChatRoom>> getChatRoom(@PathVariable Long roomId) {
        return chatService.getChatRoom(roomId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/rooms/user/{userId}")
    public Flux<ChatRoom> getUserChatRooms(@PathVariable Long userId) {
        return chatService.getUserChatRooms(userId);
    }

    @DeleteMapping("/rooms/{roomId}")
    public Mono<ResponseEntity<Void>> deleteChatRoom(@PathVariable Long roomId) {
        return chatService.deleteChatRoom(roomId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/rooms/{roomId}/members/{userId}")
    public Mono<ResponseEntity<Void>> addMember(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        return chatService.addUserToChatRoom(roomId, userId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/rooms/{roomId}/members/{userId}")
    public Mono<ResponseEntity<Void>> removeMember(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        return chatService.removeUserFromChatRoom(roomId, userId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/rooms/{roomId}/members")
    public Flux<Long> getRoomMembers(@PathVariable Long roomId) {
        return chatService.getChatRoomMembers(roomId);
    }

    @PostMapping("/rooms/{roomId}/messages")
    public Mono<ResponseEntity<ChatMessage>> sendMessage(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId,
            @RequestBody String content) {
        return chatService.sendMessage(roomId, userId, content)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @GetMapping("/rooms/{roomId}/messages")
    public Flux<ChatMessage> getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "100") int limit) {
        return chatService.getRecentMessages(roomId, limit);
    }

    @GetMapping(value = "/rooms/{roomId}/messages/stream", 
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessage> streamMessages(@PathVariable Long roomId) {
        return chatService.subscribeToRoom(roomId);
    }

    @DeleteMapping("/messages/{messageId}")
    public Mono<ResponseEntity<Void>> deleteMessage(@PathVariable Long messageId) {
        return chatService.deleteMessage(messageId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/rooms/order/{orderId}")
    public Mono<ResponseEntity<ChatRoom>> getOrderChatRoom(@PathVariable Long orderId) {
        return chatService.getOrderChatRoom(orderId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/rooms/{roomId}/unread")
    public Mono<ResponseEntity<Long>> getUnreadCount(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId) {
        return chatService.getUnreadMessageCount(roomId, userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
} 