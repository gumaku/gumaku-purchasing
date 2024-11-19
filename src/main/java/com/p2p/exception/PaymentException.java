package com.p2p.exception;

public class PaymentException extends BusinessException {
    
    public PaymentException(String message) {
        super("PAYMENT_ERROR", message);
    }

    public static class PaymentNotFoundException extends PaymentException {
        public PaymentNotFoundException(Long paymentId) {
            super(String.format("Payment with id %d not found", paymentId));
        }
    }

    public static class PaymentFailedException extends PaymentException {
        public PaymentFailedException(String message) {
            super(message);
        }
    }

    public static class RefundNotAllowedException extends PaymentException {
        public RefundNotAllowedException(Long paymentId) {
            super(String.format("Payment with id %d cannot be refunded", paymentId));
        }
    }
} 