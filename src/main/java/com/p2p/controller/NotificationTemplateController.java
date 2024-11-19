package com.p2p.controller;

import com.p2p.domain.NotificationTemplate;
import com.p2p.service.NotificationTemplateService;
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
@RequestMapping("/api/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    @GetMapping("/{templateCode}")
    public Mono<ResponseEntity<String>> getTemplate(@PathVariable String templateCode) {
        return templateService.getTemplateContent(templateCode)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<NotificationTemplate> getAllTemplates() {
        return templateService.getAllTemplates();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{templateCode}")
    public Mono<ResponseEntity<Void>> updateTemplate(
            @PathVariable String templateCode,
            @RequestBody String content) {
        return templateService.updateTemplate(templateCode, content)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{templateCode}")
    public Mono<ResponseEntity<Void>> deleteTemplate(@PathVariable String templateCode) {
        return templateService.deleteTemplate(templateCode)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping("/{templateCode}/render")
    public Mono<ResponseEntity<String>> renderTemplate(
            @PathVariable String templateCode,
            @RequestBody Map<String, String> params) {
        return templateService.renderTemplate(templateCode, params)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/recent")
    public Flux<NotificationTemplate> getRecentTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        return templateService.getRecentlyUpdatedTemplates(limit);
    }

    @GetMapping("/type/{type}")
    public Flux<NotificationTemplate> getTemplatesByType(@PathVariable String type) {
        return templateService.getTemplatesByType(type);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cache/refresh")
    public Mono<ResponseEntity<Void>> refreshCache() {
        return templateService.refreshCache()
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cache/clear")
    public Mono<ResponseEntity<Void>> clearCache() {
        return templateService.clearCache()
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
} 