package com.p2p.service.impl;

import com.p2p.domain.ChatMessage;
import com.p2p.domain.ChatRoom;
import com.p2p.repository.ChatMessageRepository;
import com.p2p.repository.ChatRoomRepository;
import com.p2p.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final Map<Long, Sinks.Many<ChatMessage>> roomSinks = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public Mono<ChatRoom> createChatRoom(String name, Long orderId) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(name);
        chatRoom.setOrderId(orderId);
        chatRoom.setCreatedAt(ZonedDateTime.now());
        return chatRoomRepository.save(chatRoom)
            .doOnSuccess(room -> createMessageSink(room.getId()));
    }

    @Override
    public Mono<ChatRoom> getChatRoom(Long id) {
        return chatRoomRepository.findById(id);
    }

    @Override
    public Flux<ChatRoom> getUserChatRooms(Long userId) {
        return chatRoomRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Mono<Void> deleteChatRoom(Long id) {
        return chatRoomRepository.deleteById(id)
            .doFinally(signalType -> roomSinks.remove(id));
    }

    @Override
    @Transactional
    public Mono<ChatMessage> sendMessage(Long roomId, Long senderId, String content) {
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(roomId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setType(ChatMessage.MessageType.TEXT);
        message.setCreatedAt(ZonedDateTime.now());
        return chatMessageRepository.save(message)
            .doOnSuccess(this::broadcastMessage);
    }

    @Override
    public Flux<ChatMessage> getRoomMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomId(roomId);
    }

    @Override
    @Transactional
    public Mono<ChatMessage> sendSystemMessage(Long roomId, String content) {
        return Mono.just(ChatMessage.createSystemMessage(roomId, content))
            .flatMap(chatMessageRepository::save)
            .doOnSuccess(this::broadcastMessage);
    }

    @Override
    @Transactional
    public Mono<ChatMessage> sendOrderStatusUpdate(Long orderId, String status) {
        return chatRoomRepository.findByOrderId(orderId)
            .flatMap(room -> sendSystemMessage(room.getId(), 
                "订单状态已更新为: " + status));
    }

    @Override
    public Flux<ChatMessage> subscribeToRoom(Long roomId) {
        return getRoomMessages(roomId)
            .mergeWith(getMessageSink(roomId).asFlux());
    }

    @Override
    public Mono<Long> getUnreadMessageCount(Long roomId, Long userId) {
        return chatMessageRepository.countUnreadMessages(roomId, userId);
    }

    @Override
    public Mono<Map<String, Object>> getChatRoomStats(Long roomId) {
        return chatMessageRepository.getChatRoomStats(roomId);
    }

    @Override
    public Mono<Map<String, Long>> getUserMessageCounts(Long roomId) {
        return chatMessageRepository.getUserMessageCounts(roomId);
    }

    @Override
    public Mono<ChatRoom> getOrderChatRoom(Long orderId) {
        return chatRoomRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Mono<Void> addUserToChatRoom(Long roomId, Long userId) {
        return chatRoomRepository.addMember(roomId, userId)
            .then(sendSystemMessage(roomId, "用户已加入聊天室"))
            .then();
    }

    @Override
    @Transactional
    public Mono<Void> removeUserFromChatRoom(Long roomId, Long userId) {
        return chatRoomRepository.removeMember(roomId, userId)
            .then(sendSystemMessage(roomId, "用户已离开聊天室"))
            .then();
    }

    @Override
    public Flux<Long> getChatRoomMembers(Long roomId) {
        return chatRoomRepository.findMembersByRoomId(roomId);
    }

    @Override
    @Transactional
    public Mono<Void> deleteMessage(Long messageId) {
        return chatMessageRepository.findById(messageId)
            .flatMap(message -> chatMessageRepository.delete(message));
    }

    @Override
    public Flux<ChatMessage> getRecentMessages(Long roomId, int limit) {
        return chatMessageRepository.findRecentMessages(roomId, limit);
    }

    private void createMessageSink(Long roomId) {
        roomSinks.computeIfAbsent(roomId, 
            k -> Sinks.many().multicast().onBackpressureBuffer());
    }

    private Sinks.Many<ChatMessage> getMessageSink(Long roomId) {
        return roomSinks.computeIfAbsent(roomId,
            k -> Sinks.many().multicast().onBackpressureBuffer());
    }

    private void broadcastMessage(ChatMessage message) {
        Sinks.Many<ChatMessage> sink = roomSinks.get(message.getChatRoomId());
        if (sink != null) {
            sink.tryEmitNext(message);
        }
    }
} 