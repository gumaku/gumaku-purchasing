package com.p2p.event;

import com.p2p.domain.Order;
import lombok.Getter;

@Getter
public class OrderEvent {
    private final Order order;
    private final OrderEventType type;
    private final Object source;

    public OrderEvent(Object source, Order order, OrderEventType type) {
        this.source = source;
        this.order = order;
        this.type = type;
    }

    public enum OrderEventType {
        CREATED,
        ACCEPTED,
        CANCELLED,
        COMPLETED,
        PAYMENT_RECEIVED
    }
} 