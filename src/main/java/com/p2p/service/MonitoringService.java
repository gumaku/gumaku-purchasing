package com.p2p.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface MonitoringService {
    // 系統指標
    Mono<Map<String, Object>> getSystemMetrics();
    Mono<Map<String, String>> getHealthStatus();
    Mono<Map<String, Object>> getDatabaseMetrics();
    
    // 性能監控
    Mono<Map<String, Object>> getPerformanceMetrics();
    Mono<Double> getSystemLoad();
    Mono<Map<String, Object>> getMemoryStats();
    
    // 用戶活動
    Mono<Long> getActiveUsersCount();
    Mono<Map<String, Long>> getActiveUsersByHour();
    
    // API監控
    Mono<Map<String, Long>> getApiUsageStats();
    Mono<Map<String, Double>> getApiResponseTimes();
    
    // 日誌查詢
    Flux<Map<String, Object>> getRecentLogs(String level, int limit);
    
    // 緩存監控
    Mono<Map<String, Object>> getCacheStats();
    Mono<Void> clearCache(String cacheName);
} 