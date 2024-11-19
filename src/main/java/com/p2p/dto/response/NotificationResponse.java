package com.p2p.dto.response;

import com.p2p.domain.Notification;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String type;
    private String content;
    private boolean read;
    private String targetUrl;
    private ZonedDateTime createdAt;

    public static NotificationResponse fromNotification(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setType(notification.getType());
        response.setContent(notification.getContent());
        response.setRead(notification.isRead());
        response.setTargetUrl(notification.getTargetUrl());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
} 