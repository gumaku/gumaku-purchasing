package com.p2p.event.listener;

import com.p2p.event.RatingEvent;
import com.p2p.service.NotificationService;
import com.p2p.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingEventListener {
    
    private final NotificationService notificationService;
    private final UserService userService;

    @Async
    @EventListener
    public void handleRatingEvent(RatingEvent event) {
        log.info("Handling rating event: {}", event.getType());
        
        switch (event.getType()) {
            case CREATED:
                handleRatingCreated(event);
                break;
            case UPDATED:
                handleRatingUpdated(event);
                break;
        }
    }

    private void handleRatingCreated(RatingEvent event) {
        // 發送通知
        notificationService.createRatingNotification(
            event.getRating().getRatedUserId(),
            "You have received a new rating",
            event.getRating().getId()
        ).subscribe();

        // 更新用戶信用評分
        userService.updateCreditScore(
            event.getRating().getRatedUserId(),
            event.getRating().getScore().doubleValue()
        ).subscribe();
    }

    private void handleRatingUpdated(RatingEvent event) {
        // 更新用戶信用評分
        userService.updateCreditScore(
            event.getRating().getRatedUserId(),
            event.getRating().getScore().doubleValue()
        ).subscribe();
    }
} 