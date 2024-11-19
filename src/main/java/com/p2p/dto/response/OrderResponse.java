package com.p2p.dto.response;

import com.p2p.domain.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
public class OrderResponse {
    private Long id;
    private Long initiatorId;
    private Long purchaserId;
    private String productName;
    private String productLink;
    private BigDecimal productPrice;
    private Integer quantity;
    private String description;
    private Order.OrderStatus status;
    private Integer minParticipants;
    private Map<Long, Integer> participants;
    private String deliveryAddress;
    private String deliveryCountry;
    private boolean verified;
    private ZonedDateTime deadline;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    
    // 額外的計算字段
    private BigDecimal totalAmount;
    private boolean expired;
    private boolean hasEnoughParticipants;
    private int currentParticipants;

    public static OrderResponse fromOrder(Order order) {
        OrderResponse response = new OrderResponse();
        // 設置基本字段...
        response.setId(order.getId());
        response.setInitiatorId(order.getInitiatorId());
        response.setPurchaserId(order.getPurchaserId());
        response.setProductName(order.getProductName());
        response.setProductLink(order.getProductLink());
        response.setProductPrice(order.getProductPrice());
        response.setQuantity(order.getQuantity());
        response.setDescription(order.getDescription());
        response.setStatus(order.getStatus());
        response.setMinParticipants(order.getMinParticipants());
        response.setParticipants(order.getParticipants());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setDeliveryCountry(order.getDeliveryCountry());
        response.setVerified(order.isVerified());
        response.setDeadline(order.getDeadline());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        // 設置計算字段
        response.setTotalAmount(order.getTotalAmount());
        response.setExpired(order.isExpired());
        response.setHasEnoughParticipants(order.hasEnoughParticipants());
        response.setCurrentParticipants(
            order.getParticipants() != null ? order.getParticipants().size() : 0
        );
        
        return response;
    }
} 