package com.p2p.service.impl;

import com.p2p.domain.KeywordSubscription;
import com.p2p.dto.response.KeywordMatchResponse;
import com.p2p.repository.KeywordSubscriptionRepository;
import com.p2p.service.KeywordSubscriptionService;
import com.p2p.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordSubscriptionServiceImpl implements KeywordSubscriptionService {

    private final KeywordSubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "keyword:subscription:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Override
    @Transactional
    public Mono<KeywordSubscription> subscribe(Long userId, String keyword) {
        return subscriptionRepository.existsByUserIdAndKeyword(userId, keyword)
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new IllegalStateException("Already subscribed to this keyword"));
                }
                KeywordSubscription subscription = new KeywordSubscription();
                subscription.setUserId(userId);
                subscription.setKeyword(keyword.toLowerCase());
                subscription.setCreatedAt(ZonedDateTime.now());
                return subscriptionRepository.save(subscription);
            });
    }

    @Override
    @Transactional
    public Mono<Void> unsubscribe(Long userId, String keyword) {
        return subscriptionRepository.findByUserIdAndKeyword(userId, keyword.toLowerCase())
            .flatMap(subscription -> subscriptionRepository.delete(subscription))
            .then(redisTemplate.opsForZSet().remove(
                CACHE_KEY_PREFIX + "user:" + userId,
                keyword.toLowerCase()
            ))
            .then();
    }

    @Override
    public Flux<KeywordSubscription> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    @Override
    public Mono<Boolean> isKeywordSubscribed(Long userId, String keyword) {
        return subscriptionRepository.existsByUserIdAndKeyword(userId, keyword.toLowerCase());
    }

    @Override
    public Flux<String> getKeywordSuggestions(String prefix, int limit) {
        return redisTemplate.opsForZSet()
            .reverseRangeWithScores(CACHE_KEY_PREFIX + "suggestions", 
                Range.closed(0L, Long.MAX_VALUE))
            .filter(tuple -> tuple.getValue() != null && 
                          tuple.getValue().toLowerCase().startsWith(prefix.toLowerCase()))
            .map(TypedTuple::getValue)
            .take(limit);
    }

    @Override
    public Flux<String> getPopularKeywords(int limit) {
        return redisTemplate.opsForZSet()
            .reverseRangeWithScores(CACHE_KEY_PREFIX + "popular", 
                Range.closed(0L, Long.MAX_VALUE))
            .map(TypedTuple::getValue)
            .take(limit);
    }

    @Override
    public Flux<String> getRelatedKeywords(String keyword, int limit) {
        return redisTemplate.opsForZSet()
            .reverseRangeWithScores(CACHE_KEY_PREFIX + "related:" + keyword.toLowerCase(), 
                Range.closed(0L, Long.MAX_VALUE))
            .map(TypedTuple::getValue)
            .take(limit);
    }

    @Override
    public Flux<String> getTrendingKeywords(int limit) {
        return redisTemplate.opsForZSet()
            .reverseRangeWithScores(CACHE_KEY_PREFIX + "trending", 
                Range.closed(0L, Long.MAX_VALUE))
            .map(TypedTuple::getValue)
            .take(limit);
    }

    @Override
    public Mono<KeywordMatchResponse> matchKeywords(String text) {
        String searchText = text.toLowerCase();
        return subscriptionRepository.findMatchingSubscriptions(searchText)
            .collectList()
            .map(subscriptions -> {
                Set<String> matchedKeywords = new HashSet<>();
                subscriptions.forEach(sub -> matchedKeywords.add(sub.getKeyword()));
                KeywordMatchResponse response = new KeywordMatchResponse();
                response.setKeyword(text);
                response.setMatchCount(matchedKeywords.size());
                response.setMatchedKeywords(new ArrayList<>(matchedKeywords));
                return response;
            })
            .doOnSuccess(response -> {
                if (!response.getMatchedKeywords().isEmpty()) {
                    subscriptionRepository.incrementMatchCount(
                        new HashSet<>(response.getMatchedKeywords())
                    ).subscribe();
                }
            });
    }

    @Override
    public Mono<Void> notifySubscribers(Long orderId, Set<String> keywords) {
        return subscriptionRepository.findUsersByKeywords(new ArrayList<>(keywords))
            .flatMap(userId -> 
                notificationService.createNotification(
                    userId,
                    String.format("發現新的匹配訂單，關鍵字：%s", String.join(", ", keywords)),
                    "KEYWORD_MATCH"
                )
            )
            .then();
    }

    @Override
    public Mono<Map<String, Object>> getKeywordStatistics() {
        return subscriptionRepository.getKeywordStatistics()
            .map(stats -> {
                Map<String, Object> result = new HashMap<>();
                result.put("keywordCounts", stats);
                return result;
            });
    }

    @Override
    public Mono<Void> cleanupUnusedKeywords() {
        return subscriptionRepository.deleteUnusedKeywords(ZonedDateTime.now().minusDays(30));
    }

    @Override
    public Mono<Void> cleanupExpiredSubscriptions(int days) {
        return subscriptionRepository.deleteExpiredSubscriptions(
            ZonedDateTime.now().minusDays(days)
        );
    }

    private Mono<Void> updateKeywordScore(String keyword, double score) {
        return redisTemplate.opsForZSet()
            .incrementScore(CACHE_KEY_PREFIX + "popular", keyword, score)
            .then();
    }

    private Mono<Void> cacheKeyword(String keyword) {
        return redisTemplate.opsForZSet()
            .add(CACHE_KEY_PREFIX + "suggestions", keyword, 0.0)
            .then();
    }
} 