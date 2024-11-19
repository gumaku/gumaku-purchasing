package com.p2p.controller;

import com.p2p.domain.Payment;
import com.p2p.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}")
    public Mono<ResponseEntity<Payment>> getPayment(
            @PathVariable Long paymentId) {
        return paymentService.getPayment(paymentId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{paymentId}/refund")
    public Mono<ResponseEntity<Payment>> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason) {
        return paymentService.initiateRefund(paymentId, reason)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/user")
    public Flux<Payment> getUserPayments(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return paymentService.getUserPayments(userId, page, size);
    }

    @GetMapping("/order/{orderId}")
    public Flux<Payment> getOrderPayments(
            @PathVariable Long orderId) {
        return paymentService.getOrderPayments(orderId);
    }

    @PostMapping("/webhook")
    public Mono<ResponseEntity<Void>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        return paymentService.handleStripeWebhook(payload, signature)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @GetMapping("/statistics")
    public Mono<ResponseEntity<Map<String, Object>>> getPaymentStatistics(
            @AuthenticationPrincipal Long userId) {
        return paymentService.getPaymentStatistics(userId)
            .map(ResponseEntity::ok);
    }
} 