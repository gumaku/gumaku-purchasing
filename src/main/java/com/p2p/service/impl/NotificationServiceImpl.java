package com.p2p.service.impl;

import com.p2p.domain.Notification;
import com.p2p.repository.NotificationRepository;
import com.p2p.service.NotificationService;
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
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final Map<Long, Sinks.Many<Notification>> userNotificationSinks = new ConcurrentHashMap<>();

    @Override
    public Mono<Notification> createNotification(Long userId, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(ZonedDateTime.now());

        return notificationRepository.save(notification)
            .doOnSuccess(this::broadcastNotification);
    }

    @Override
    public Mono<Notification> createOrderNotification(Long userId, String content, Long orderId) {
        return Mono.just(Notification.createOrderNotification(userId, content, orderId))
            .flatMap(notificationRepository::save)
            .doOnSuccess(this::broadcastNotification);
    }

    @Override
    public Mono<Notification> createPaymentNotification(Long userId, String content, Long paymentId) {
        return Mono.just(Notification.createPaymentNotification(userId, content, paymentId))
            .flatMap(notificationRepository::save)
            .doOnSuccess(this::broadcastNotification);
    }

    @Override
    public Mono<Notification> createRatingNotification(Long userId, String content, Long ratingId) {
        return Mono.just(Notification.createRatingNotification(userId, content, ratingId))
            .flatMap(notificationRepository::save)
            .doOnSuccess(this::broadcastNotification);
    }

    @Override
    public Flux<Notification> getUserNotifications(Long userId, boolean unreadOnly) {
        return unreadOnly 
            ? notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false)
            : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Mono<Long> getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    @Override
    @Transactional
    public Mono<Notification> markAsRead(Long notificationId) {
        return notificationRepository.findById(notificationId)
            .flatMap(notification -> {
                notification.markAsRead();
                return notificationRepository.save(notification);
            });
    }

    @Override
    @Transactional
    public Mono<Void> markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public Mono<Void> deleteNotification(Long notificationId, Long userId) {
        return notificationRepository.findById(notificationId)
            .filter(notification -> notification.getUserId().equals(userId))
            .flatMap(notification -> notificationRepository.delete(notification));
    }

    @Override
    @Transactional
    public Mono<Void> deleteAllNotifications(Long userId) {
        return notificationRepository.deleteByUserId(userId);
    }

    @Override
    public Flux<Notification> subscribeToUserNotifications(Long userId) {
        return Flux.create(sink -> {
            Sinks.Many<Notification> userSink = userNotificationSinks.computeIfAbsent(
                userId,
                k -> Sinks.many().multicast().onBackpressureBuffer()
            );
            userSink.asFlux().subscribe(sink::next, sink::error, sink::complete);
        });
    }

    @Override
    public Mono<Void> cleanupInactiveSinks() {
        userNotificationSinks.clear();
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteOldNotifications(int daysOld) {
        ZonedDateTime cutoffDate = ZonedDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteByCreatedAtBeforeAndRead(cutoffDate, true)
            .doOnSuccess(v -> log.info("Deleted old notifications before {}", cutoffDate));
    }

    private void broadcastNotification(Notification notification) {
        Sinks.Many<Notification> sink = userNotificationSinks.get(notification.getUserId());
        if (sink != null) {
            sink.tryEmitNext(notification);
        }
    }
} 