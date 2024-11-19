package com.p2p.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Table("orders")
public class Order {
    @Id
    private Long id;
    private Long initiatorId;
    private Long purchaserId;
    private String productName;
    private String productLink;
    private BigDecimal productPrice;
    private Integer quantity;
    private String description;
    private String deliveryAddress;
    private String deliveryCountry;
    private OrderStatus status;
    private String cancelReason;
    private boolean verified;
    private Integer minParticipants;
    private Map<Long, Integer> participants = new HashMap<>();
    private ZonedDateTime deadline;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public enum OrderStatus {
        PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public void setCancelReason(String reason) {
        this.cancelReason = reason;
        this.updatedAt = ZonedDateTime.now();
    }

    public BigDecimal getTotalAmount() {
        return productPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isExpired() {
        return deadline != null && ZonedDateTime.now().isAfter(deadline);
    }

    public boolean hasEnoughParticipants() {
        return minParticipants == null || 
               participants.size() >= minParticipants;
    }

    public void addParticipant(Long userId, Integer quantity) {
        participants.put(userId, quantity);
        this.updatedAt = ZonedDateTime.now();
    }

    public void removeParticipant(Long userId) {
        participants.remove(userId);
        this.updatedAt = ZonedDateTime.now();
    }

    public Map<Long, Integer> getParticipants() {
        return participants;
    }
} 