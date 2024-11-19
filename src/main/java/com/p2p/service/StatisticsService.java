package com.p2p.service;

import com.p2p.dto.statistics.OrderStatistics;
import com.p2p.dto.statistics.UserStatistics;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

public interface StatisticsService {
    // 订单统计
    Mono<OrderStatistics> getOrderStatistics(LocalDate startDate, LocalDate endDate);
    Mono<Map<String, Long>> getOrderCountByStatus();
    Mono<Map<String, Long>> getOrderCountByMonth();
    
    // 用户统计
    Mono<UserStatistics> getUserStatistics(LocalDate startDate, LocalDate endDate);
    Mono<Map<String, Long>> getUserCountByRole();
    Mono<Map<String, Long>> getActiveUsersByDay();
    Mono<Map<String, Long>> getActiveUsersByHour();
    Mono<Map<String, Double>> getUserCreditScoreDistribution();
    
    // 交易统计
    Mono<Map<String, Object>> getTransactionStatistics(LocalDate startDate, LocalDate endDate);
    Mono<Map<String, Object>> getRevenueByPeriod(String period);
    
    // 报表生成
    Mono<byte[]> generateOrderReport(LocalDate startDate, LocalDate endDate);
    Mono<byte[]> generateUserReport(LocalDate startDate, LocalDate endDate);
    Mono<byte[]> generateFinancialReport(LocalDate startDate, LocalDate endDate);
} 