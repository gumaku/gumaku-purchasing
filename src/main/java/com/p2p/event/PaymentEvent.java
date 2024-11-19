package com.p2p.event;

import com.p2p.domain.Payment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentEvent extends ApplicationEvent {
    private final Payment payment;
    private final PaymentEventType type;

    public PaymentEvent(Object source, Payment payment, PaymentEventType type) {
        super(source);
        this.payment = payment;
        this.type = type;
    }

    public enum PaymentEventType {
        CREATED,
        COMPLETED,
        FAILED,
        REFUNDED
    }
} 