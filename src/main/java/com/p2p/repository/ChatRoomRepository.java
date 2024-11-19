package com.p2p.repository;

import com.p2p.domain.ChatRoom;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRoomRepository extends R2dbcRepository<ChatRoom, Long> {
    
    Mono<ChatRoom> findByOrderId(Long orderId);

    @Query("SELECT cr.* FROM chat_rooms cr " +
           "JOIN chat_room_members crm ON cr.id = crm.chat_room_id " +
           "WHERE crm.user_id = :userId " +
           "ORDER BY cr.updated_at DESC")
    Flux<ChatRoom> findByUserId(@Param("userId") Long userId);

    @Query("INSERT INTO chat_room_members (chat_room_id, user_id) " +
           "VALUES (:roomId, :userId)")
    Mono<Void> addMember(
        @Param("roomId") Long roomId,
        @Param("userId") Long userId
    );

    @Query("DELETE FROM chat_room_members " +
           "WHERE chat_room_id = :roomId AND user_id = :userId")
    Mono<Void> removeMember(
        @Param("roomId") Long roomId,
        @Param("userId") Long userId
    );

    @Query("SELECT user_id FROM chat_room_members " +
           "WHERE chat_room_id = :roomId")
    Flux<Long> findMembersByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT EXISTS(SELECT 1 FROM chat_room_members " +
           "WHERE chat_room_id = :roomId AND user_id = :userId)")
    Mono<Boolean> isMember(
        @Param("roomId") Long roomId,
        @Param("userId") Long userId
    );

    @Query("UPDATE chat_rooms " +
           "SET last_message_at = CURRENT_TIMESTAMP " +
           "WHERE id = :roomId")
    Mono<Void> updateLastMessageTime(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(*) FROM chat_room_members " +
           "WHERE chat_room_id = :roomId")
    Mono<Long> getMemberCount(@Param("roomId") Long roomId);

    @Query("SELECT cr.* FROM chat_rooms cr " +
           "WHERE cr.id = :roomId " +
           "AND EXISTS (SELECT 1 FROM chat_room_members " +
           "           WHERE chat_room_id = :roomId " +
           "           AND user_id = :userId)")
    Mono<ChatRoom> findByIdAndMemberId(
        @Param("roomId") Long roomId,
        @Param("userId") Long userId
    );
} 