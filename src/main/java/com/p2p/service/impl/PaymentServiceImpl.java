package com.p2p.service.impl;

import com.p2p.domain.Payment;
import com.p2p.event.PaymentEvent;
import com.p2p.repository.PaymentRepository;
import com.p2p.service.NotificationService;
import com.p2p.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    @Transactional
    public Mono<PaymentIntent> createPaymentIntent(Long orderId, Long userId, BigDecimal amount) {
        return Mono.fromCallable(() -> {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(new BigDecimal(100)).longValue())
                .setCurrency("hkd")
                .putMetadata("orderId", orderId.toString())
                .putMetadata("userId", userId.toString())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setUserId(userId);
            payment.setStripePaymentId(intent.getId());
            payment.setAmount(amount);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setCreatedAt(ZonedDateTime.now());

            return paymentRepository.save(payment)
                .thenReturn(intent)
                .block();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Payment> getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public Mono<Boolean> isPaymentCompleted(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .any(payment -> payment.getStatus() == Payment.PaymentStatus.COMPLETED);
    }

    @Override
    public Mono<Boolean> isPaymentExpired(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .map(payment -> 
                payment.getCreatedAt().plusHours(24).isBefore(ZonedDateTime.now())
            );
    }

    @Override
    public Mono<Long> countSuccessfulPayments() {
        return paymentRepository.countByStatus(Payment.PaymentStatus.COMPLETED);
    }

    @Override
    public Flux<Payment> getUserPayments(Long userId, int page, int size) {
        return paymentRepository.findByUserId(userId)
            .skip(page * size)
            .take(size);
    }

    @Override
    public Flux<Payment> getOrderPayments(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public Flux<Payment> getRecentPayments(int limit) {
        return paymentRepository.findByOrderByCreatedAtDesc()
            .take(limit);
    }

    @Override
    public Mono<Map<String, Object>> getPaymentStatistics(Long userId) {
        return Mono.zip(
            paymentRepository.countByStatus(Payment.PaymentStatus.COMPLETED),
            paymentRepository.sumUserTotalAmount(userId),
            paymentRepository.calculateAverageAmount()
        ).map(tuple -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPayments", tuple.getT1());
            stats.put("totalAmount", tuple.getT2());
            stats.put("averageAmount", tuple.getT3());
            return stats;
        });
    }

    @Override
    public Mono<BigDecimal> getUserTotalAmount(Long userId) {
        return paymentRepository.sumUserTotalAmount(userId)
            .defaultIfEmpty(BigDecimal.ZERO);
    }

    @Override
    public Mono<Boolean> canInitiateRefund(Long paymentId, Long userId) {
        return paymentRepository.findById(paymentId)
            .map(payment -> 
                payment.getUserId().equals(userId) && 
                payment.getStatus() == Payment.PaymentStatus.COMPLETED &&
                payment.getCreatedAt().plusDays(7).isAfter(ZonedDateTime.now())
            )
            .defaultIfEmpty(false);
    }

    @Override
    @Transactional
    public Mono<Payment> initiateRefund(Long paymentId, String reason) {
        return paymentRepository.findById(paymentId)
            .filter(payment -> payment.getStatus() == Payment.PaymentStatus.COMPLETED)
            .flatMap(payment -> Mono.fromCallable(() -> {
                Map<String, Object> params = new HashMap<>();
                params.put("payment_intent", payment.getStripePaymentId());
                params.put("reason", reason);
                
                Refund refund = Refund.create(params);
                
                if ("succeeded".equals(refund.getStatus())) {
                    payment.setStatus(Payment.PaymentStatus.REFUNDED);
                    payment.setRefundReason(reason);
                    payment.setUpdatedAt(ZonedDateTime.now());
                    
                    return paymentRepository.save(payment)
                        .doOnSuccess(savedPayment -> {
                            eventPublisher.publishEvent(
                                new PaymentEvent(this, savedPayment, 
                                    PaymentEvent.PaymentEventType.REFUNDED)
                            );
                            notificationService.createPaymentNotification(
                                savedPayment.getUserId(),
                                "退款已處理：" + reason,
                                savedPayment.getId()
                            ).subscribe();
                        })
                        .block();
                } else {
                    throw new RuntimeException("Refund failed: " + refund.getFailureReason());
                }
            }).subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    @Transactional
    public Mono<Payment> confirmPayment(String paymentIntentId) {
        return Mono.fromCallable(() -> {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return paymentRepository.findByStripePaymentId(paymentIntentId)
                .flatMap(payment -> {
                    if ("succeeded".equals(intent.getStatus())) {
                        payment.setStatus(Payment.PaymentStatus.COMPLETED);
                        payment.setUpdatedAt(ZonedDateTime.now());
                        return paymentRepository.save(payment)
                            .doOnSuccess(savedPayment -> {
                                eventPublisher.publishEvent(
                                    new PaymentEvent(this, savedPayment, 
                                        PaymentEvent.PaymentEventType.COMPLETED)
                                );
                                notificationService.createPaymentNotification(
                                    savedPayment.getUserId(),
                                    "支付成功",
                                    savedPayment.getId()
                                ).subscribe();
                            });
                    } else {
                        payment.setStatus(Payment.PaymentStatus.FAILED);
                        payment.setUpdatedAt(ZonedDateTime.now());
                        return paymentRepository.save(payment)
                            .doOnSuccess(savedPayment -> {
                                eventPublisher.publishEvent(
                                    new PaymentEvent(this, savedPayment, 
                                        PaymentEvent.PaymentEventType.FAILED)
                                );
                            });
                    }
                })
                .block();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Payment> refundPayment(Long paymentId) {
        return initiateRefund(paymentId, "Customer requested refund");
    }

    @Override
    public Mono<Void> handleStripeWebhook(String payload, String signature) {
        return Mono.fromCallable(() -> {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent intent = (PaymentIntent) event.getData().getObject();
                    return confirmPayment(intent.getId()).block();
                case "payment_intent.payment_failed":
                    // 處理支付失敗
                    log.error("Payment failed: {}", event.getData().getObject());
                    break;
                case "charge.refunded":
                    // 處理退款
                    log.info("Payment refunded: {}", event.getData().getObject());
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
            return null;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }
} 