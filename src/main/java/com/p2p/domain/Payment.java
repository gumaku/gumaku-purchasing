package com.p2p.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Table("payments")
public class Payment {
    @Id
    private Long id;
    private Long orderId;
    private Long userId;
    private String stripePaymentId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private PaymentStatus status;
    private String refundReason;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED,
        CANCELLED
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.updatedAt = ZonedDateTime.now();
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
        this.updatedAt = ZonedDateTime.now();
    }

    public void setRefundReason(String reason) {
        this.refundReason = reason;
        this.updatedAt = ZonedDateTime.now();
    }
} 