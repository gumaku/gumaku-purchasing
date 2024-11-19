package com.p2p.repository;

import com.p2p.domain.KeywordSubscription;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface KeywordSubscriptionRepository extends R2dbcRepository<KeywordSubscription, Long> {
    Mono<Boolean> existsByUserIdAndKeyword(Long userId, String keyword);
    Flux<KeywordSubscription> findByUserId(Long userId);
    @Query("SELECT * FROM keyword_subscriptions WHERE " +
           "LOWER(:text) LIKE CONCAT('%', LOWER(keyword), '%')")
    Flux<KeywordSubscription> findMatchingSubscriptions(@Param("text") String text);
    @Query("SELECT DISTINCT user_id FROM keyword_subscriptions " +
           "WHERE keyword IN (:keywords)")
    Flux<Long> findUsersByKeywords(@Param("keywords") List<String> keywords);
    @Query("DELETE FROM keyword_subscriptions " +
           "WHERE created_at < :date AND " +
           "NOT EXISTS (SELECT 1 FROM orders WHERE " +
           "LOWER(product_name) LIKE CONCAT('%', LOWER(keyword), '%') OR " +
           "LOWER(description) LIKE CONCAT('%', LOWER(keyword), '%'))")
    Mono<Void> deleteUnusedKeywords(@Param("date") ZonedDateTime date);
    @Query("DELETE FROM keyword_subscriptions WHERE created_at < :date")
    Mono<Void> deleteExpiredSubscriptions(@Param("date") ZonedDateTime date);
    @Query("SELECT keyword, COUNT(*) as count FROM keyword_subscriptions " +
           "GROUP BY keyword ORDER BY count DESC")
    Mono<Map<String, Long>> getKeywordStatistics();
    @Query("UPDATE keyword_statistics SET match_count = match_count + 1 " +
           "WHERE keyword IN (:keywords)")
    Mono<Void> incrementMatchCount(@Param("keywords") Set<String> keywords);
    @Query("SELECT keyword, match_count FROM keyword_statistics " +
           "WHERE keyword IN (:keywords)")
    Flux<KeywordMatchCount> getMatchCounts(@Param("keywords") Set<String> keywords);
    interface KeywordMatchCount {
        String getKeyword();
        Long getMatchCount();
    }
    @Query("INSERT INTO keyword_statistics (keyword, match_count) " +
           "VALUES (:keyword, 1) " +
           "ON CONFLICT (keyword) DO UPDATE " +
           "SET match_count = keyword_statistics.match_count + 1")
    Mono<Void> upsertMatchCount(@Param("keyword") String keyword);
    Mono<KeywordSubscription> findByUserIdAndKeyword(Long userId, String keyword);
    @Query("DELETE FROM keyword_subscriptions " +
           "WHERE user_id = :userId AND keyword = :keyword")
    Mono<Void> deleteByUserIdAndKeyword(
        @Param("userId") Long userId,
        @Param("keyword") String keyword
    );
} 