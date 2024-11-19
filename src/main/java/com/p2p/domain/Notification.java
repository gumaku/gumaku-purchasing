package com.p2p.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Table("notifications")
public class Notification {
    @Id
    private Long id;
    private Long userId;
    private String content;
    private String type;
    private Long targetId;
    private String targetUrl;
    private boolean read;
    private ZonedDateTime createdAt;
    private ZonedDateTime readAt;

    public void markAsRead() {
        this.read = true;
        this.readAt = ZonedDateTime.now();
    }

    public static Notification createOrderNotification(Long userId, String content, Long orderId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setType("ORDER");
        notification.setTargetId(orderId);
        notification.setTargetUrl("/orders/" + orderId);
        notification.setRead(false);
        notification.setCreatedAt(ZonedDateTime.now());
        return notification;
    }

    public static Notification createPaymentNotification(Long userId, String content, Long paymentId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setType("PAYMENT");
        notification.setTargetId(paymentId);
        notification.setTargetUrl("/payments/" + paymentId);
        notification.setRead(false);
        notification.setCreatedAt(ZonedDateTime.now());
        return notification;
    }

    public static Notification createRatingNotification(Long userId, String content, Long ratingId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setType("RATING");
        notification.setTargetId(ratingId);
        notification.setTargetUrl("/ratings/" + ratingId);
        notification.setRead(false);
        notification.setCreatedAt(ZonedDateTime.now());
        return notification;
    }

    public static Notification createSystemNotification(Long userId, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setType("SYSTEM");
        notification.setRead(false);
        notification.setCreatedAt(ZonedDateTime.now());
        return notification;
    }
} 