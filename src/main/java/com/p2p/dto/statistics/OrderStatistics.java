package com.p2p.dto.statistics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderStatistics {
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> ordersByMonth;
    private BigDecimal totalTransactionAmount;
    private BigDecimal averageOrderAmount;
    private Long totalOrders;
    private Map<String, Long> popularCategories;
    private Map<String, BigDecimal> revenueByMonth;
} 