package com.p2p.service;

import com.p2p.domain.SystemConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface SystemConfigService {
    Mono<String> getConfigValue(String key);
    Mono<SystemConfig> getConfig(String key);
    Flux<SystemConfig> getAllConfigs();
    Mono<Void> updateConfig(String key, String value);
    Mono<Void> updateConfigs(Map<String, String> configs);
    Mono<Void> deleteConfig(String key);
    Mono<Void> refreshCache();
    Mono<Void> clearCache();
    
    // 類型轉換方法
    Mono<Integer> getIntConfig(String key);
    Mono<Double> getDoubleConfig(String key);
    Mono<Boolean> getBooleanConfig(String key);
} 