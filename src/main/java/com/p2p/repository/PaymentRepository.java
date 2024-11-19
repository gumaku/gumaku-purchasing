package com.p2p.repository;

import com.p2p.domain.Payment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentRepository extends R2dbcRepository<Payment, Long> {
    
    Mono<Payment> findByStripePaymentId(String stripePaymentId);
    
    Mono<Long> countByStatus(Payment.PaymentStatus status);

    @Query("SELECT COUNT(*) FROM payments WHERE status = 'COMPLETED'")
    Mono<Long> countSuccessfulPayments();

    @Query("SELECT AVG(amount) FROM payments WHERE status = 'COMPLETED'")
    Mono<BigDecimal> calculateAverageAmount();

    @Query("SELECT SUM(amount) FROM payments " +
           "WHERE status = 'COMPLETED' AND user_id = :userId")
    Mono<BigDecimal> sumUserTotalAmount(@Param("userId") Long userId);

    @Query("DELETE FROM payments WHERE created_at < :date")
    Mono<Void> deleteByCreatedAtBefore(@Param("date") LocalDate date);

    Flux<Payment> findByUserId(Long userId);
    
    Flux<Payment> findByOrderId(Long orderId);
    
    Flux<Payment> findByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(*) FROM payments WHERE status = :status")
    Mono<Long> countByPaymentStatus(@Param("status") String status);
} 