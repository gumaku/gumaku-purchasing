package com.p2p.repository;

import com.p2p.domain.NotificationTemplate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationTemplateRepository extends R2dbcRepository<NotificationTemplate, Long> {
    
    Mono<NotificationTemplate> findByTemplateCode(String templateCode);
    
    @Query("DELETE FROM notification_templates WHERE template_code = :templateCode")
    Mono<Void> deleteByTemplateCode(@Param("templateCode") String templateCode);
    
    @Query("SELECT * FROM notification_templates WHERE active = true")
    Flux<NotificationTemplate> findAllActive();
    
    @Query("UPDATE notification_templates " +
           "SET active = :active, updated_at = CURRENT_TIMESTAMP " +
           "WHERE template_code = :templateCode")
    Mono<Void> updateActiveStatus(
        @Param("templateCode") String templateCode,
        @Param("active") boolean active
    );
    
    @Query("SELECT * FROM notification_templates " +
           "WHERE type = :type AND active = true")
    Flux<NotificationTemplate> findByType(@Param("type") String type);
    
    @Query("SELECT COUNT(*) FROM notification_templates " +
           "WHERE type = :type AND active = true")
    Mono<Long> countByType(@Param("type") String type);
} 