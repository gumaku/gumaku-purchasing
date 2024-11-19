package com.p2p.service;

import com.p2p.domain.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
    // 基本通知操作
    Mono<Notification> createNotification(Long userId, String content, String type);
    Mono<Notification> createOrderNotification(Long userId, String content, Long orderId);
    Mono<Notification> createPaymentNotification(Long userId, String content, Long paymentId);
    Mono<Notification> createRatingNotification(Long userId, String content, Long ratingId);
    
    // 通知查询
    Flux<Notification> getUserNotifications(Long userId, boolean unreadOnly);
    Mono<Long> getUnreadCount(Long userId);
    
    // 通知状态管理
    Mono<Notification> markAsRead(Long notificationId);
    Mono<Void> markAllAsRead(Long userId);
    Mono<Void> deleteNotification(Long notificationId, Long userId);
    Mono<Void> deleteAllNotifications(Long userId);
    
    // 实时订阅
    Flux<Notification> subscribeToUserNotifications(Long userId);
    Mono<Void> cleanupInactiveSinks();
    
    // 系统维护
    Mono<Void> deleteOldNotifications(int daysOld);
} 