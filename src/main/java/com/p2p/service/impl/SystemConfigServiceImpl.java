package com.p2p.service.impl;

import com.p2p.domain.SystemConfig;
import com.p2p.repository.SystemConfigRepository;
import com.p2p.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository configRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String CACHE_PREFIX = "system:config:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Override
    public Mono<String> getConfigValue(String key) {
        return redisTemplate.opsForValue().get(CACHE_PREFIX + key)
            .switchIfEmpty(
                configRepository.findByKey(key)
                    .map(SystemConfig::getValue)
                    .flatMap(value -> cacheConfig(key, value)
                        .thenReturn(value))
            );
    }

    @Override
    public Mono<SystemConfig> getConfig(String key) {
        return configRepository.findByKey(key);
    }

    @Override
    public Flux<SystemConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    @Override
    @Transactional
    public Mono<Void> updateConfig(String key, String value) {
        return configRepository.findByKey(key)
            .flatMap(config -> {
                config.setValue(value);
                return configRepository.save(config);
            })
            .switchIfEmpty(
                configRepository.save(new SystemConfig(key, value))
            )
            .flatMap(config -> cacheConfig(key, value))
            .then();
    }

    @Override
    @Transactional
    public Mono<Void> updateConfigs(Map<String, String> configs) {
        return Flux.fromIterable(configs.entrySet())
            .flatMap(entry -> updateConfig(entry.getKey(), entry.getValue()))
            .then();
    }

    @Override
    @Transactional
    public Mono<Void> deleteConfig(String key) {
        return configRepository.deleteByKey(key)
            .then(redisTemplate.opsForValue().delete(CACHE_PREFIX + key))
            .then();
    }

    @Override
    public Mono<Void> refreshCache() {
        return getAllConfigs()
            .flatMap(config -> cacheConfig(config.getKey(), config.getValue()))
            .then();
    }

    @Override
    public Mono<Void> clearCache() {
        return redisTemplate.keys(CACHE_PREFIX + "*")
            .flatMap(redisTemplate.opsForValue()::delete)
            .then();
    }

    @Override
    public Mono<Integer> getIntConfig(String key) {
        return getConfigValue(key)
            .map(Integer::parseInt);
    }

    @Override
    public Mono<Double> getDoubleConfig(String key) {
        return getConfigValue(key)
            .map(Double::parseDouble);
    }

    @Override
    public Mono<Boolean> getBooleanConfig(String key) {
        return getConfigValue(key)
            .map(Boolean::parseBoolean);
    }

    private Mono<Void> cacheConfig(String key, String value) {
        return redisTemplate.opsForValue()
            .set(CACHE_PREFIX + key, value, CACHE_TTL)
            .then();
    }
} 