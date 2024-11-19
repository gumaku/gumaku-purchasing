package com.p2p.dto.order;

import com.p2p.domain.Order;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSearchRequest {
    private String keyword;
    private Order.OrderStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private int page = 0;
    private int size = 20;
} 