package com.p2p.controller;

import com.p2p.domain.KeywordSubscription;
import com.p2p.service.KeywordSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordSubscriptionController {

    private final KeywordSubscriptionService subscriptionService;

    @PostMapping("/subscribe/{keyword}")
    public Mono<ResponseEntity<KeywordSubscription>> subscribe(
            @AuthenticationPrincipal Long userId,
            @PathVariable String keyword) {
        return subscriptionService.subscribe(userId, keyword)
            .map(ResponseEntity::ok)
            .doOnSuccess(response -> 
                log.info("User {} subscribed to keyword: {}", userId, keyword));
    }

    @DeleteMapping("/unsubscribe/{keyword}")
    public Mono<ResponseEntity<Void>> unsubscribe(
            @AuthenticationPrincipal Long userId,
            @PathVariable String keyword) {
        return subscriptionService.unsubscribe(userId, keyword)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .doOnSuccess(response -> 
                log.info("User {} unsubscribed from keyword: {}", userId, keyword));
    }

    @GetMapping("/my-subscriptions")
    public Flux<KeywordSubscription> getUserSubscriptions(
            @AuthenticationPrincipal Long userId) {
        return subscriptionService.getUserSubscriptions(userId);
    }

    @GetMapping("/check/{keyword}")
    public Mono<ResponseEntity<Boolean>> isSubscribed(
            @AuthenticationPrincipal Long userId,
            @PathVariable String keyword) {
        return subscriptionService.isKeywordSubscribed(userId, keyword)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/suggestions")
    public Flux<String> getKeywordSuggestions(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "10") int limit) {
        return subscriptionService.getKeywordSuggestions(prefix, limit);
    }

    @GetMapping("/popular")
    public Flux<String> getPopularKeywords(
            @RequestParam(defaultValue = "10") int limit) {
        return subscriptionService.getPopularKeywords(limit);
    }

    @GetMapping("/statistics")
    public Mono<ResponseEntity<Map<String, Object>>> getKeywordStatistics() {
        return subscriptionService.getKeywordStatistics()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/related/{keyword}")
    public Flux<String> getRelatedKeywords(
            @PathVariable String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        return subscriptionService.getRelatedKeywords(keyword, limit);
    }

    @GetMapping("/trending")
    public Flux<String> getTrendingKeywords(
            @RequestParam(defaultValue = "10") int limit) {
        return subscriptionService.getTrendingKeywords(limit);
    }
} 