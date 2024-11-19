package com.p2p.repository;

import com.p2p.domain.Notification;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

public interface NotificationRepository extends R2dbcRepository<Notification, Long> {
    
    Flux<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Flux<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, boolean read);
    
    Mono<Long> countByUserIdAndRead(Long userId, boolean read);
    
    @Query("UPDATE notifications SET read = true WHERE user_id = :userId")
    Mono<Void> markAllAsRead(@Param("userId") Long userId);
    
    @Query("DELETE FROM notifications WHERE user_id = :userId")
    Mono<Void> deleteByUserId(@Param("userId") Long userId);
    
    @Query("DELETE FROM notifications " +
           "WHERE created_at < :date AND read = :read")
    Mono<Void> deleteByCreatedAtBeforeAndRead(
        @Param("date") ZonedDateTime date,
        @Param("read") boolean read
    );
    
    @Query("SELECT user_id FROM notification_subscriptions " +
           "WHERE keyword = :keyword")
    Flux<Long> findUsersByMatchingKeyword(@Param("keyword") String keyword);
} 