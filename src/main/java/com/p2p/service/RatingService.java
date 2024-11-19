package com.p2p.service;

import com.p2p.domain.Rating;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface RatingService {
    // 評分管理
    Mono<Rating> createRating(Rating rating);
    Flux<Rating> getUserRatings(Long userId, int page, int size);
    Mono<Double> getUserAverageRating(Long userId);
    Mono<Rating> getOrderRating(Long orderId, Long raterId);
    
    // 評分查詢
    Flux<Rating> getLatestRatings(Long userId, int limit);
    Mono<Map<Integer, Long>> getRatingDistribution(Long userId);
    
    // 評分驗證
    Mono<Boolean> canRateOrder(Long orderId, Long raterId);
    Mono<Rating> updateRating(Long ratingId, Rating rating);
    Mono<Void> deleteRating(Long ratingId, Long userId);
    
    // 統計功能
    Mono<Long> countUserRatings(Long userId);
    Mono<Map<String, Double>> getRatingAveragesByType(Long userId);
    Mono<Long> countHighRatings(Long userId, int minScore);
} 