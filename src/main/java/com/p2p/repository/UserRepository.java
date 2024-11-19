package com.p2p.repository;

import com.p2p.domain.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

public interface UserRepository extends R2dbcRepository<User, Long> {
    
    Mono<User> findByEmail(String email);
    
    Mono<Boolean> existsByEmail(String email);

    @Query("SELECT COUNT(*) FROM users " +
           "WHERE created_at BETWEEN :startDate AND :endDate")
    Mono<Long> countTotalUsers(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(*) FROM users " +
           "WHERE last_login_at BETWEEN :startDate AND :endDate")
    Mono<Long> countActiveUsers(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT DATE_TRUNC('month', created_at) as month, COUNT(*) as count " +
           "FROM users " +
           "GROUP BY month " +
           "ORDER BY month")
    Mono<Map<String, Long>> countUsersByMonth();

    @Query("SELECT role, COUNT(*) as count " +
           "FROM users " +
           "GROUP BY role")
    Mono<Map<String, Long>> countUsersByRole();

    @Query("SELECT " +
           "CASE " +
           "  WHEN credit_score < 60 THEN 'low' " +
           "  WHEN credit_score < 80 THEN 'medium' " +
           "  ELSE 'high' " +
           "END as range, " +
           "COUNT(*) as count " +
           "FROM users " +
           "WHERE credit_score IS NOT NULL " +
           "GROUP BY range")
    Mono<Map<String, Double>> calculateCreditScoreDistribution();

    @Query("SELECT DATE_TRUNC('day', last_login_at) as day, COUNT(*) as count " +
           "FROM users " +
           "WHERE last_login_at BETWEEN :startDate AND :endDate " +
           "GROUP BY day " +
           "ORDER BY day")
    Mono<Map<String, Long>> countActiveUsersByDay(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT AVG(credit_score) as average_rating " +
           "FROM users " +
           "WHERE credit_score IS NOT NULL")
    Mono<Double> calculateAverageRating();

    @Query("SELECT EXTRACT(HOUR FROM last_login_at) as hour, COUNT(*) as count " +
           "FROM users " +
           "WHERE last_login_at BETWEEN :startDate AND :endDate " +
           "GROUP BY hour " +
           "ORDER BY hour")
    Mono<Map<String, Long>> calculateUserActivityDistributionBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("WITH cohort AS ( " +
           "  SELECT DATE_TRUNC('month', created_at) as cohort_month, " +
           "         COUNT(DISTINCT CASE WHEN last_login_at >= created_at + INTERVAL '30 days' " +
           "                            THEN id END)::float / " +
           "         COUNT(DISTINCT id) as retention_rate " +
           "  FROM users " +
           "  WHERE created_at BETWEEN :startDate AND :endDate " +
           "  GROUP BY cohort_month " +
           ") " +
           "SELECT AVG(retention_rate) as avg_retention_rate " +
           "FROM cohort")
    Mono<Double> calculateUserRetentionRateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(*) FROM users " +
           "WHERE roles @> ARRAY[:role]::varchar[]")
    Mono<Long> countByRolesContaining(@Param("role") String role);
} 