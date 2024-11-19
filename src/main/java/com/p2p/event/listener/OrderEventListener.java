package com.p2p.event.listener;

import com.p2p.event.OrderEvent;
import com.p2p.service.KeywordSubscriptionService;
import com.p2p.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;
    private final KeywordSubscriptionService keywordSubscriptionService;

    @EventListener
    public void handleOrderEvent(OrderEvent event) {
        switch (event.getType()) {
            case CREATED -> handleOrderCreated(event);
            case ACCEPTED -> handleOrderAccepted(event);
            case CANCELLED -> handleOrderCancelled(event);
            case COMPLETED -> handleOrderCompleted(event);
            case PAYMENT_RECEIVED -> handlePaymentReceived(event);
        }
    }

    private void handleOrderCreated(OrderEvent event) {
        var order = event.getOrder();
        Set<String> keywords = Set.of(
            order.getProductName().toLowerCase(),
            order.getDescription().toLowerCase()
        );
        
        keywordSubscriptionService.notifySubscribers(order.getId(), keywords)
            .subscribe(
                null,
                error -> log.error("Error notifying subscribers for order {}", order.getId(), error)
            );
    }

    private void handleOrderAccepted(OrderEvent event) {
        var order = event.getOrder();
        notificationService.createOrderNotification(
            order.getInitiatorId(),
            "您的訂單已被接受",
            order.getId()
        ).subscribe(
            null,
            error -> log.error("Error sending order accepted notification", error)
        );
    }

    private void handleOrderCancelled(OrderEvent event) {
        var order = event.getOrder();
        notificationService.createOrderNotification(
            order.getInitiatorId(),
            "您的訂單已被取消",
            order.getId()
        ).subscribe(
            null,
            error -> log.error("Error sending order cancelled notification", error)
        );
    }

    private void handleOrderCompleted(OrderEvent event) {
        var order = event.getOrder();
        notificationService.createOrderNotification(
            order.getInitiatorId(),
            "您的訂單已完成",
            order.getId()
        ).subscribe(
            null,
            error -> log.error("Error sending order completed notification", error)
        );
    }

    private void handlePaymentReceived(OrderEvent event) {
        var order = event.getOrder();
        notificationService.createOrderNotification(
            order.getInitiatorId(),
            "訂單付款已收到",
            order.getId()
        ).subscribe(
            null,
            error -> log.error("Error sending payment received notification", error)
        );
    }
} 