package com.p2p.repository;

import com.p2p.domain.Rating;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface RatingRepository extends R2dbcRepository<Rating, Long> {
    
    Flux<Rating> findByRatedUserIdOrderByCreatedAtDesc(Long ratedUserId);
    
    Mono<Rating> findByOrderIdAndRaterId(Long orderId, Long raterId);
    
    Mono<Boolean> existsByOrderIdAndRaterId(Long orderId, Long raterId);

    @Query("SELECT COUNT(*) FROM ratings WHERE rated_user_id = :userId")
    Mono<Long> countByRatedUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(score) FROM ratings WHERE rated_user_id = :userId")
    Mono<Double> calculateAverageRating(@Param("userId") Long userId);

    @Query("SELECT score, COUNT(*) as count " +
           "FROM ratings " +
           "WHERE rated_user_id = :userId " +
           "GROUP BY score")
    Mono<Map<Integer, Long>> getRatingDistribution(@Param("userId") Long userId);

    @Query("SELECT r.* FROM ratings r " +
           "JOIN orders o ON r.order_id = o.id " +
           "WHERE o.status = 'COMPLETED' " +
           "AND r.rated_user_id = :userId " +
           "ORDER BY r.created_at DESC " +
           "LIMIT :limit")
    Flux<Rating> findCompletedOrderRatings(
        @Param("userId") Long userId,
        @Param("limit") int limit
    );

    @Query("SELECT COUNT(*) FROM ratings " +
           "WHERE rated_user_id = :userId " +
           "AND score >= :minScore")
    Mono<Long> countHighRatings(
        @Param("userId") Long userId,
        @Param("minScore") int minScore
    );

    @Query("SELECT type, AVG(score) as average_score " +
           "FROM ratings " +
           "WHERE rated_user_id = :userId " +
           "GROUP BY type")
    Flux<RatingTypeAverage> getRatingAveragesByType(@Param("userId") Long userId);

    interface RatingTypeAverage {
        String getType();
        Double getAverageScore();
    }
} 