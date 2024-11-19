package com.p2p.repository;

import com.p2p.domain.Order;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface OrderRepository extends R2dbcRepository<Order, Long> {
    
    Flux<Order> findByInitiatorId(Long initiatorId);
    
    Flux<Order> findByPurchaserId(Long purchaserId);
    
    Flux<Order> findByStatus(Order.OrderStatus status);
    
    Flux<Order> findByOrderByCreatedAtDesc();

    Mono<Long> countByStatus(Order.OrderStatus status);

    @Query("SELECT AVG(CAST(product_price AS DECIMAL) * quantity) FROM orders")
    Mono<BigDecimal> calculateAverageOrderAmount();

    @Query("SELECT status, COUNT(*) as count FROM orders GROUP BY status")
    Mono<Map<String, Long>> countOrdersByStatus();

    @Query("SELECT DATE_TRUNC('month', created_at) as month, COUNT(*) as count " +
           "FROM orders " +
           "WHERE created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY month " +
           "ORDER BY month")
    Mono<Map<String, Long>> countOrdersByMonth(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(CAST(product_price AS DECIMAL) * quantity) " +
           "FROM orders " +
           "WHERE created_at BETWEEN :startDate AND :endDate")
    Mono<BigDecimal> calculateTotalTransactionAmount(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT category, COUNT(*) as count " +
           "FROM orders " +
           "WHERE created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY category")
    Mono<Map<String, Long>> countOrdersByCategory(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT DATE_TRUNC('month', created_at) as month, " +
           "SUM(CAST(product_price AS DECIMAL) * quantity) as revenue " +
           "FROM orders " +
           "WHERE created_at BETWEEN :startDate AND :endDate " +
           "GROUP BY month " +
           "ORDER BY month")
    Mono<Map<String, BigDecimal>> calculateRevenueByMonth(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("DELETE FROM orders WHERE created_at < :date")
    Mono<Void> deleteByCreatedAtBefore(@Param("date") LocalDate date);

    @Query("SELECT * FROM orders " +
           "WHERE LOWER(product_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND (:status IS NULL OR status = :status) " +
           "AND (:minPrice IS NULL OR product_price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR product_price <= :maxPrice)")
    Flux<Order> searchOrders(
        @Param("keyword") String keyword,
        @Param("status") Order.OrderStatus status,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice
    );
} 