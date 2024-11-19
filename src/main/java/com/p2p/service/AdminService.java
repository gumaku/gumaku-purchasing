package com.p2p.service;

import com.p2p.dto.statistics.OrderStatistics;
import com.p2p.dto.statistics.UserStatistics;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

public interface AdminService {
    // 统计数据
    Mono<OrderStatistics> getOrderStatistics(LocalDate startDate, LocalDate endDate);
    Mono<UserStatistics> getUserStatistics(LocalDate startDate, LocalDate endDate);
    
    // 系统管理
    Mono<Map<String, Object>> getSystemStatus();
    Mono<Map<String, Object>> getDatabaseStatus();
    
    // 用户管理
    Mono<Void> blockUser(Long userId);
    Mono<Void> unblockUser(Long userId);
    
    // 订单管理
    Mono<Void> verifyOrder(Long orderId);
    Mono<Void> cancelOrder(Long orderId, String reason);
    
    // 系统维护
    Mono<Void> cleanupOldData(LocalDate before);
    Mono<Void> refreshCache();
} 