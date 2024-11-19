package com.p2p.service;

import com.p2p.domain.NotificationTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface NotificationTemplateService {
    // 模板内容管理
    Mono<String> getTemplateContent(String templateCode);
    Flux<NotificationTemplate> getAllTemplates();
    Mono<Void> updateTemplate(String templateCode, String content);
    Mono<Void> deleteTemplate(String templateCode);
    
    // 模板查询
    Flux<NotificationTemplate> getRecentlyUpdatedTemplates(int limit);
    Flux<NotificationTemplate> getTemplatesByType(String type);
    
    // 模板渲染
    Mono<String> renderTemplate(String templateCode, Map<String, String> params);
    
    // 缓存管理
    Mono<Void> refreshCache();
    Mono<Void> clearCache();
} 