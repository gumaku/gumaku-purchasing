package com.p2p.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class DashboardStats {
    private long totalUsers;
    private long activeUsers;
    private long totalOrders;
    private long pendingOrders;
    private BigDecimal totalTransactions;
    private double averageRating;
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> usersByRole;
    private Map<String, BigDecimal> revenueByMonth;
    private double systemUptime;
    private int onlineUsers;
    private Map<String, Long> dailyActiveUsers;
} 