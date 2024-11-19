package com.p2p.event.listener;

import com.p2p.event.PaymentEvent;
import com.p2p.service.NotificationService;
import com.p2p.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;
    private final NotificationService notificationService;

    @EventListener
    public void handlePaymentEvent(PaymentEvent event) {
        switch (event.getType()) {
            case COMPLETED -> handlePaymentCompleted(event);
            case FAILED -> handlePaymentFailed(event);
            case REFUNDED -> handlePaymentRefunded(event);
        }
    }

    private void handlePaymentCompleted(PaymentEvent event) {
        orderService.updateOrderAfterPayment(event.getPayment().getOrderId())
            .flatMap(order -> 
                notificationService.createOrderNotification(
                    order.getInitiatorId(),
                    "訂單支付成功",
                    order.getId()
                )
            )
            .subscribe(
                null,
                error -> log.error("Error handling payment completed event", error)
            );
    }

    private void handlePaymentFailed(PaymentEvent event) {
        notificationService.createOrderNotification(
            event.getPayment().getUserId(),
            "訂單支付失敗",
            event.getPayment().getOrderId()
        ).subscribe(
            null,
            error -> log.error("Error handling payment failed event", error)
        );
    }

    private void handlePaymentRefunded(PaymentEvent event) {
        notificationService.createOrderNotification(
            event.getPayment().getUserId(),
            "訂單已退款",
            event.getPayment().getOrderId()
        ).subscribe(
            null,
            error -> log.error("Error handling payment refunded event", error)
        );
    }
} 