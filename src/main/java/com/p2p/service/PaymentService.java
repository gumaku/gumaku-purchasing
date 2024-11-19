package com.p2p.service;

import com.p2p.domain.Payment;
import com.stripe.model.PaymentIntent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    // 支付基本操作
    Mono<PaymentIntent> createPaymentIntent(Long orderId, Long userId, BigDecimal amount);
    Mono<Payment> confirmPayment(String paymentIntentId);
    Mono<Payment> getPayment(Long paymentId);
    
    // 支付狀態查詢
    Mono<Boolean> isPaymentCompleted(Long orderId);
    Mono<Boolean> isPaymentExpired(Long paymentId);
    Mono<Long> countSuccessfulPayments();
    
    // 退款相關
    Mono<Payment> initiateRefund(Long paymentId, String reason);
    Mono<Boolean> canInitiateRefund(Long paymentId, Long userId);
    Mono<Payment> refundPayment(Long paymentId);
    
    // 用戶支付查詢
    Flux<Payment> getUserPayments(Long userId, int page, int size);
    Flux<Payment> getOrderPayments(Long orderId);
    Flux<Payment> getRecentPayments(int limit);
    
    // 統計功能
    Mono<Map<String, Object>> getPaymentStatistics(Long userId);
    Mono<BigDecimal> getUserTotalAmount(Long userId);
    
    // Webhook處理
    Mono<Void> handleStripeWebhook(String payload, String signature);
} 