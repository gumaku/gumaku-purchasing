package com.p2p.repository;

import com.p2p.domain.SystemConfig;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface SystemConfigRepository extends R2dbcRepository<SystemConfig, Long> {
    
    Mono<SystemConfig> findByKey(String key);

    @Query("UPDATE system_configs SET value = :value, updated_at = CURRENT_TIMESTAMP " +
           "WHERE key = :key")
    Mono<Void> updateValue(
        @Param("key") String key,
        @Param("value") String value
    );

    @Query("DELETE FROM system_configs WHERE key = :key")
    Mono<Void> deleteByKey(@Param("key") String key);
} 