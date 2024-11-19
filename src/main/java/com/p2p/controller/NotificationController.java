package com.p2p.controller;

import com.p2p.domain.Notification;
import com.p2p.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Notification> subscribeToNotifications(
            @AuthenticationPrincipal Long userId) {
        return notificationService.subscribeToUserNotifications(userId)
            .doOnSubscribe(subscription -> 
                log.info("User {} subscribed to notifications", userId))
            .doOnCancel(() -> 
                log.info("User {} unsubscribed from notifications", userId));
    }

    @GetMapping
    public Flux<Notification> getNotifications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return notificationService.getUserNotifications(userId, unreadOnly);
    }

    @GetMapping("/unread/count")
    public Mono<ResponseEntity<Long>> getUnreadCount(
            @AuthenticationPrincipal Long userId) {
        return notificationService.getUnreadCount(userId)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/{notificationId}/read")
    public Mono<ResponseEntity<Notification>> markAsRead(
            @PathVariable Long notificationId) {
        return notificationService.markAsRead(notificationId)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/read-all")
    public Mono<ResponseEntity<Void>> markAllAsRead(
            @AuthenticationPrincipal Long userId) {
        return notificationService.markAllAsRead(userId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @DeleteMapping("/{notificationId}")
    public Mono<ResponseEntity<Void>> deleteNotification(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId) {
        return notificationService.deleteNotification(notificationId, userId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @DeleteMapping
    public Mono<ResponseEntity<Void>> deleteAllNotifications(
            @AuthenticationPrincipal Long userId) {
        return notificationService.deleteAllNotifications(userId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
} 