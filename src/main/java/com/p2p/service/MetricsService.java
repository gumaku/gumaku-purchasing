package com.p2p.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // API請求計數
    public void recordApiCall(String path, String method) {
        Counter.builder("api.requests")
            .tag("path", path)
            .tag("method", method)
            .register(meterRegistry)
            .increment();
    }

    // API響應時間
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String path) {
        sample.stop(Timer.builder("api.response.time")
            .tag("path", path)
            .register(meterRegistry));
    }

    // 用戶活動
    public void recordUserActivity(Long userId, String activity) {
        Counter.builder("user.activity")
            .tag("userId", userId.toString())
            .tag("activity", activity)
            .register(meterRegistry)
            .increment();
    }

    // 訂單統計
    public void recordOrderCreation() {
        Counter.builder("orders.created")
            .register(meterRegistry)
            .increment();
    }

    public void recordOrderCompletion() {
        Counter.builder("orders.completed")
            .register(meterRegistry)
            .increment();
    }

    // 支付統計
    public void recordPayment(double amount) {
        Counter.builder("payments.total")
            .register(meterRegistry)
            .increment(amount);
    }

    // 系統性能
    public void recordDatabaseQueryTime(long milliseconds) {
        Timer.builder("database.query.time")
            .register(meterRegistry)
            .record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public void recordCacheHit(String cache) {
        Counter.builder("cache.hits")
            .tag("cache", cache)
            .register(meterRegistry)
            .increment();
    }

    public void recordCacheMiss(String cache) {
        Counter.builder("cache.misses")
            .tag("cache", cache)
            .register(meterRegistry)
            .increment();
    }
} 