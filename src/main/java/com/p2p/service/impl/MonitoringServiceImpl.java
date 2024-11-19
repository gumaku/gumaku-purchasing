package com.p2p.service.impl;

import com.p2p.service.MonitoringService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    private final MeterRegistry meterRegistry;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Map<String, Object>> getSystemMetrics() {
        return Mono.fromCallable(() -> {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("heap_used", heapUsage.getUsed());
            metrics.put("thread_count", Thread.activeCount());
            metrics.put("system_load", getSystemLoad().block());
            return metrics;
        });
    }

    @Override
    public Mono<Map<String, Object>> getDatabaseMetrics() {
        return databaseClient.sql("SELECT " +
                                "pg_database_size(current_database()) as db_size, " +
                                "count(*) as active_connections " +
                                "FROM pg_stat_activity")
            .fetch()
            .first()
            .map(row -> {
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("database_size", row.get("db_size"));
                metrics.put("active_connections", row.get("active_connections"));
                return metrics;
            })
            .flatMap(metrics -> 
                databaseClient.sql("SELECT schemaname, relname, n_live_tup " +
                                 "FROM pg_stat_user_tables " +
                                 "ORDER BY n_live_tup DESC LIMIT 5")
                    .fetch()
                    .all()
                    .collectList()
                    .map(tables -> {
                        metrics.put("top_tables", tables);
                        return metrics;
                    })
            );
    }

    @Override
    public Mono<Map<String, String>> getHealthStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        return checkRedisHealth()
            .map(redisHealth -> {
                status.put("redis", redisHealth ? "UP" : "DOWN");
                return status;
            });
    }

    @Override
    public Mono<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("average_response_time", 
            meterRegistry.get("http.server.requests").timer().mean(TimeUnit.MILLISECONDS));
        metrics.put("total_requests", 
            meterRegistry.get("http.server.requests").timer().count());
        return Mono.just(metrics);
    }

    @Override
    public Mono<Double> getSystemLoad() {
        return Mono.fromCallable(() -> 
            ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
    }

    @Override
    public Mono<Map<String, Object>> getMemoryStats() {
        return Mono.fromCallable(() -> {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("heap_used", heapUsage.getUsed());
            stats.put("heap_max", heapUsage.getMax());
            stats.put("non_heap_used", nonHeapUsage.getUsed());
            stats.put("non_heap_max", nonHeapUsage.getMax());
            stats.put("heap_usage_percentage", 
                (double) heapUsage.getUsed() / heapUsage.getMax() * 100);
            stats.put("non_heap_usage_percentage", 
                (double) nonHeapUsage.getUsed() / nonHeapUsage.getMax() * 100);
            return stats;
        });
    }

    @Override
    public Mono<Long> getActiveUsersCount() {
        return redisTemplate.opsForValue()
            .get("active_users_count")
            .map(Long::parseLong)
            .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Map<String, Long>> getActiveUsersByHour() {
        return redisTemplate.opsForHash()
            .entries("active_users_by_hour")
            .collectMap(
                entry -> entry.getKey().toString(),
                entry -> Long.parseLong(entry.getValue().toString())
            );
    }

    @Override
    public Mono<Map<String, Long>> getApiUsageStats() {
        return redisTemplate.opsForHash()
            .entries("api_usage_stats")
            .collectMap(
                entry -> entry.getKey().toString(),
                entry -> Long.parseLong(entry.getValue().toString())
            );
    }

    @Override
    public Mono<Map<String, Double>> getApiResponseTimes() {
        return redisTemplate.opsForHash()
            .entries("api_response_times")
            .collectMap(
                entry -> entry.getKey().toString(),
                entry -> Double.parseDouble(entry.getValue().toString())
            );
    }

    @Override
    public Flux<Map<String, Object>> getRecentLogs(String level, int limit) {
        return redisTemplate.opsForList()
            .range("logs:" + level, 0, limit - 1)
            .map(log -> {
                Map<String, Object> logEntry = new HashMap<>();
                // 假設日誌格式為 JSON 字符串
                // 這裡需要實現具體的解析邏輯
                logEntry.put("raw", log);
                return logEntry;
            });
    }

    @Override
    public Mono<Map<String, Object>> getCacheStats() {
        return redisTemplate.getConnectionFactory()
            .getReactiveConnection()
            .serverCommands()
            .info()
            .map(info -> {
                Map<String, Object> stats = new HashMap<>();
                stats.put("used_memory", info.getProperty("used_memory"));
                stats.put("connected_clients", info.getProperty("connected_clients"));
                stats.put("total_commands_processed", info.getProperty("total_commands_processed"));
                return stats;
            });
    }

    @Override
    public Mono<Void> clearCache(String cacheName) {
        return redisTemplate.keys(cacheName + ":*")
            .flatMap(redisTemplate.opsForValue()::delete)
            .then();
    }

    private Mono<Boolean> checkRedisHealth() {
        return redisTemplate.hasKey("health_check");
    }
} 