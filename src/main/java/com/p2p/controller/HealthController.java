package com.p2p.controller;

import com.p2p.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final MonitoringService monitoringService;

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> checkHealth() {
        return monitoringService.getHealthStatus()
            .map(status -> {
                Map<String, Object> response = new HashMap<>();
                response.put("status", status.get("status"));
                response.put("timestamp", System.currentTimeMillis());
                response.put("services", status);
                return ResponseEntity.ok(response);
            })
            .onErrorResume(e -> {
                log.error("Health check failed", e);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "DOWN");
                response.put("timestamp", System.currentTimeMillis());
                response.put("error", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().body(response));
            });
    }

    @GetMapping("/liveness")
    public Mono<ResponseEntity<Map<String, Object>>> checkLiveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/readiness")
    public Mono<ResponseEntity<Map<String, Object>>> checkReadiness() {
        return monitoringService.getSystemMetrics()
            .map(metrics -> {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "UP");
                response.put("timestamp", System.currentTimeMillis());
                response.put("metrics", metrics);
                return ResponseEntity.ok(response);
            })
            .onErrorResume(e -> {
                log.error("Readiness check failed", e);
                Map<String, Object> response = new HashMap<>();
                response.put("status", "DOWN");
                response.put("timestamp", System.currentTimeMillis());
                response.put("error", e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().body(response));
            });
    }

    @GetMapping("/metrics")
    public Mono<ResponseEntity<Map<String, Object>>> getMetrics() {
        return monitoringService.getSystemMetrics()
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
} 