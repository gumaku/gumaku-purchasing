package com.p2p.service.impl;

import com.p2p.domain.NotificationTemplate;
import com.p2p.repository.NotificationTemplateRepository;
import com.p2p.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String CACHE_PREFIX = "notification:template:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Override
    public Mono<String> getTemplateContent(String templateCode) {
        return redisTemplate.opsForValue().get(CACHE_PREFIX + templateCode)
            .switchIfEmpty(
                templateRepository.findByTemplateCode(templateCode)
                    .map(NotificationTemplate::getContent)
                    .flatMap(content -> cacheTemplate(templateCode, content)
                        .thenReturn(content))
            );
    }

    @Override
    public Flux<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    @Override
    @Transactional
    public Mono<Void> updateTemplate(String templateCode, String content) {
        return templateRepository.findByTemplateCode(templateCode)
            .flatMap(template -> {
                template.setContent(content);
                return templateRepository.save(template);
            })
            .switchIfEmpty(
                templateRepository.save(NotificationTemplate.create(templateCode, content))
            )
            .flatMap(template -> cacheTemplate(templateCode, template.getContent()))
            .then();
    }

    @Override
    @Transactional
    public Mono<Void> deleteTemplate(String templateCode) {
        return templateRepository.deleteByTemplateCode(templateCode)
            .then(redisTemplate.opsForValue().delete(CACHE_PREFIX + templateCode))
            .then();
    }

    @Override
    public Mono<String> renderTemplate(String templateCode, Map<String, String> params) {
        return getTemplateContent(templateCode)
            .map(content -> replaceParams(content, params));
    }

    @Override
    public Mono<Void> refreshCache() {
        return getAllTemplates()
            .flatMap(template -> 
                cacheTemplate(template.getTemplateCode(), template.getContent())
            )
            .then();
    }

    @Override
    public Mono<Void> clearCache() {
        return redisTemplate.keys(CACHE_PREFIX + "*")
            .flatMap(redisTemplate.opsForValue()::delete)
            .then()
            .doOnSuccess(v -> log.info("Notification template cache cleared"));
    }

    @Override
    public Flux<NotificationTemplate> getRecentlyUpdatedTemplates(int limit) {
        return templateRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))
            .take(limit);
    }

    @Override
    public Flux<NotificationTemplate> getTemplatesByType(String type) {
        return templateRepository.findByType(type);
    }

    private Mono<Void> cacheTemplate(String templateCode, String content) {
        return redisTemplate.opsForValue()
            .set(CACHE_PREFIX + templateCode, content, CACHE_TTL)
            .then();
    }

    private String replaceParams(String template, Map<String, String> params) {
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
} 