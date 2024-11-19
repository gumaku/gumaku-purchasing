package com.p2p.exception;

public class OrderException extends BusinessException {
    
    public OrderException(String message) {
        super("ORDER_ERROR", message);
    }

    public static class OrderNotFoundException extends OrderException {
        public OrderNotFoundException(Long orderId) {
            super(String.format("Order with id %d not found", orderId));
        }
    }

    public static class OrderExpiredException extends OrderException {
        public OrderExpiredException(Long orderId) {
            super(String.format("Order with id %d has expired", orderId));
        }
    }

    public static class InvalidOrderStatusException extends OrderException {
        public InvalidOrderStatusException(String message) {
            super(message);
        }
    }

    public static class InsufficientParticipantsException extends OrderException {
        public InsufficientParticipantsException(Long orderId) {
            super(String.format("Order with id %d does not have enough participants", orderId));
        }
    }
} 