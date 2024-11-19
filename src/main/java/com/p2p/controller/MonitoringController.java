package com.p2p.controller;

import com.p2p.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/system")
    public Mono<ResponseEntity<Map<String, Object>>> getSystemMetrics() {
        return monitoringService.getSystemMetrics()
            .map(ResponseEntity::ok)
            .doOnSuccess(response -> log.info("System metrics retrieved"));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> getHealthStatus() {
        return monitoringService.getHealthStatus()
            .map(ResponseEntity::ok)
            .doOnSuccess(response -> log.info("Health status checked"));
    }

    @GetMapping("/database")
    public Mono<ResponseEntity<Map<String, Object>>> getDatabaseMetrics() {
        return monitoringService.getDatabaseMetrics()
            .map(ResponseEntity::ok)
            .doOnSuccess(response -> log.info("Database metrics retrieved"));
    }

    @GetMapping("/performance")
    public Mono<ResponseEntity<Map<String, Object>>> getPerformanceMetrics() {
        return monitoringService.getPerformanceMetrics()
            .map(ResponseEntity::ok)
            .doOnSuccess(response -> log.info("Performance metrics retrieved"));
    }

    @GetMapping("/memory")
    public Mono<ResponseEntity<Map<String, Object>>> getMemoryStats() {
        return monitoringService.getMemoryStats()
            .map(ResponseEntity::ok)
            .doOnSuccess(response -> log.info("Memory stats retrieved"));
    }

    @GetMapping("/users/active")
    public Mono<ResponseEntity<Long>> getActiveUsersCount() {
        return monitoringService.getActiveUsersCount()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/users/active/hourly")
    public Mono<ResponseEntity<Map<String, Long>>> getActiveUsersByHour() {
        return monitoringService.getActiveUsersByHour()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/api/usage")
    public Mono<ResponseEntity<Map<String, Long>>> getApiUsageStats() {
        return monitoringService.getApiUsageStats()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/api/response-times")
    public Mono<ResponseEntity<Map<String, Double>>> getApiResponseTimes() {
        return monitoringService.getApiResponseTimes()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/logs")
    public Flux<Map<String, Object>> getRecentLogs(
            @RequestParam(defaultValue = "INFO") String level,
            @RequestParam(defaultValue = "100") int limit) {
        return monitoringService.getRecentLogs(level, limit);
    }

    @GetMapping("/cache/stats")
    public Mono<ResponseEntity<Map<String, Object>>> getCacheStats() {
        return monitoringService.getCacheStats()
            .map(ResponseEntity::ok);
    }

    @PostMapping("/cache/{cacheName}/clear")
    public Mono<ResponseEntity<Void>> clearCache(@PathVariable String cacheName) {
        return monitoringService.clearCache(cacheName)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .doOnSuccess(response -> log.info("Cache {} cleared", cacheName));
    }
} 