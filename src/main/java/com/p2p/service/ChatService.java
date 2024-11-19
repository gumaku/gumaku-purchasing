package com.p2p.service;

import com.p2p.domain.ChatMessage;
import com.p2p.domain.ChatRoom;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ChatService {
    // 聊天室管理
    Mono<ChatRoom> createChatRoom(String name, Long orderId);
    Mono<ChatRoom> getChatRoom(Long id);
    Mono<ChatRoom> getOrderChatRoom(Long orderId);
    Flux<ChatRoom> getUserChatRooms(Long userId);
    Mono<Void> deleteChatRoom(Long id);
    
    // 成员管理
    Mono<Void> addUserToChatRoom(Long roomId, Long userId);
    Mono<Void> removeUserFromChatRoom(Long roomId, Long userId);
    Flux<Long> getChatRoomMembers(Long roomId);
    
    // 消息管理
    Mono<ChatMessage> sendMessage(Long roomId, Long senderId, String content);
    Flux<ChatMessage> getRoomMessages(Long roomId);
    Flux<ChatMessage> getRecentMessages(Long roomId, int limit);
    Mono<Void> deleteMessage(Long messageId);
    Mono<ChatMessage> sendSystemMessage(Long roomId, String content);
    Mono<ChatMessage> sendOrderStatusUpdate(Long orderId, String status);
    
    // 实时订阅
    Flux<ChatMessage> subscribeToRoom(Long roomId);
    Mono<Long> getUnreadMessageCount(Long roomId, Long userId);
    
    // 聊天室统计
    Mono<Map<String, Object>> getChatRoomStats(Long roomId);
    Mono<Map<String, Long>> getUserMessageCounts(Long roomId);
} 