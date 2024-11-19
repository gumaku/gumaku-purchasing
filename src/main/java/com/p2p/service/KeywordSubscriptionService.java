package com.p2p.service;

import com.p2p.domain.KeywordSubscription;
import com.p2p.dto.response.KeywordMatchResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

public interface KeywordSubscriptionService {
    // 訂閱管理
    Mono<KeywordSubscription> subscribe(Long userId, String keyword);
    Mono<Void> unsubscribe(Long userId, String keyword);
    Flux<KeywordSubscription> getUserSubscriptions(Long userId);
    Mono<Boolean> isKeywordSubscribed(Long userId, String keyword);
    
    // 關鍵字查詢
    Flux<String> getKeywordSuggestions(String prefix, int limit);
    Flux<String> getPopularKeywords(int limit);
    Flux<String> getRelatedKeywords(String keyword, int limit);
    Flux<String> getTrendingKeywords(int limit);
    
    // 關鍵字匹配
    Mono<KeywordMatchResponse> matchKeywords(String text);
    Mono<Void> notifySubscribers(Long orderId, Set<String> keywords);
    
    // 統計功能
    Mono<Map<String, Object>> getKeywordStatistics();
    
    // 系統維護
    Mono<Void> cleanupUnusedKeywords();
    Mono<Void> cleanupExpiredSubscriptions(int days);
} 