package com.p2p.controller;

import com.p2p.domain.SystemConfig;
import com.p2p.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService configService;

    @GetMapping("/{key}")
    public Mono<ResponseEntity<String>> getConfigValue(@PathVariable String key) {
        return configService.getConfigValue(key)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<SystemConfig> getAllConfigs() {
        return configService.getAllConfigs();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{key}")
    public Mono<ResponseEntity<Void>> updateConfig(
            @PathVariable String key,
            @RequestParam String value) {
        return configService.updateConfig(key, value)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .doOnSuccess(v -> log.info("Updated config: {} = {}", key, value));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/batch")
    public Mono<ResponseEntity<Void>> updateConfigs(
            @Valid @RequestBody Map<String, String> configs) {
        return configService.updateConfigs(configs)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .doOnSuccess(v -> log.info("Updated {} configs", configs.size()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cache/refresh")
    public Mono<ResponseEntity<Void>> refreshCache() {
        return configService.refreshCache()
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .doOnSuccess(v -> log.info("Refreshed config cache"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cache/clear")
    public Mono<ResponseEntity<Void>> clearCache() {
        return configService.clearCache()
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .doOnSuccess(v -> log.info("Cleared config cache"));
    }

    @GetMapping("/typed/{key}")
    public Mono<ResponseEntity<Object>> getTypedConfig(
            @PathVariable String key,
            @RequestParam(required = false, defaultValue = "string") String type) {
        Mono<?> value = switch (type.toLowerCase()) {
            case "int" -> configService.getIntConfig(key);
            case "double" -> configService.getDoubleConfig(key);
            case "boolean" -> configService.getBooleanConfig(key);
            default -> configService.getConfigValue(key);
        };
        
        return value
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> getConfigHealth() {
        Map<String, Object> health = new HashMap<>();
        return configService.getAllConfigs()
            .collectList()
            .map(configs -> {
                health.put("totalConfigs", configs.size());
                health.put("status", "UP");
                health.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(health);
            });
    }
} 