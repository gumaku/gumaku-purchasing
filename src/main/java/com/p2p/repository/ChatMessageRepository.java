package com.p2p.repository;

import com.p2p.domain.ChatMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface ChatMessageRepository extends R2dbcRepository<ChatMessage, Long> {
    
    Flux<ChatMessage> findByChatRoomId(Long chatRoomId);
    
    @Query("SELECT * FROM chat_messages " +
           "WHERE chat_room_id = :chatRoomId " +
           "ORDER BY created_at DESC " +
           "LIMIT :limit")
    Flux<ChatMessage> findRecentMessages(
        @Param("chatRoomId") Long chatRoomId,
        @Param("limit") int limit
    );

    @Query("SELECT COUNT(*) FROM chat_messages " +
           "WHERE chat_room_id = :chatRoomId " +
           "AND sender_id != :userId " +
           "AND created_at > (SELECT last_read_at FROM chat_room_members " +
           "                  WHERE chat_room_id = :chatRoomId AND user_id = :userId)")
    Mono<Long> countUnreadMessages(
        @Param("chatRoomId") Long chatRoomId,
        @Param("userId") Long userId
    );

    @Query("SELECT " +
           "COUNT(*) as total_messages, " +
           "COUNT(DISTINCT sender_id) as unique_senders, " +
           "MAX(created_at) as last_message_at " +
           "FROM chat_messages " +
           "WHERE chat_room_id = :chatRoomId")
    Mono<Map<String, Object>> getChatRoomStats(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT sender_id, COUNT(*) as message_count " +
           "FROM chat_messages " +
           "WHERE chat_room_id = :chatRoomId " +
           "GROUP BY sender_id")
    Mono<Map<String, Long>> getUserMessageCounts(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT * FROM chat_messages " +
           "WHERE chat_room_id = :chatRoomId " +
           "AND type = 'SYSTEM' " +
           "ORDER BY created_at DESC " +
           "LIMIT :limit")
    Flux<ChatMessage> findSystemMessages(
        @Param("chatRoomId") Long chatRoomId,
        @Param("limit") int limit
    );
} 