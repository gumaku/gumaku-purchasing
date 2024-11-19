package com.p2p.service.impl;

import com.p2p.dto.statistics.OrderStatistics;
import com.p2p.dto.statistics.UserStatistics;
import com.p2p.repository.*;
import com.p2p.service.AdminService;
import com.p2p.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<OrderStatistics> getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        return Mono.zip(
            orderRepository.countOrdersByStatus(),
            orderRepository.countOrdersByMonth(startDate, endDate),
            orderRepository.calculateTotalTransactionAmount(startDate, endDate),
            orderRepository.calculateAverageOrderAmount(),
            orderRepository.count(),
            orderRepository.countOrdersByCategory(startDate, endDate),
            orderRepository.calculateRevenueByMonth(startDate, endDate)
        ).map(tuple -> {
            OrderStatistics stats = new OrderStatistics();
            stats.setOrdersByStatus(tuple.getT1());
            stats.setOrdersByMonth(tuple.getT2());
            stats.setTotalTransactionAmount(tuple.getT3());
            stats.setAverageOrderAmount(tuple.getT4());
            stats.setTotalOrders(tuple.getT5());
            stats.setPopularCategories(tuple.getT6());
            stats.setRevenueByMonth(tuple.getT7());
            return stats;
        });
    }

    @Override
    public Mono<UserStatistics> getUserStatistics(LocalDate startDate, LocalDate endDate) {
        return Mono.zip(
            userRepository.countTotalUsers(startDate, endDate),
            userRepository.countActiveUsers(startDate, endDate),
            userRepository.countUsersByMonth(),
            userRepository.countUsersByRole(),
            userRepository.calculateCreditScoreDistribution(),
            userRepository.calculateUserActivityDistributionBetween(startDate, endDate),
            userRepository.calculateUserRetentionRateBetween(startDate, endDate)
        ).map(tuple -> {
            UserStatistics stats = new UserStatistics();
            stats.setTotalUsers(tuple.getT1());
            stats.setActiveUsers(tuple.getT2());
            stats.setUsersByMonth(tuple.getT3());
            stats.setUsersByRole(tuple.getT4());
            stats.setCreditScoreDistribution(tuple.getT5());
            stats.setUserActivityDistribution(tuple.getT6());
            stats.setRetentionRate(tuple.getT7());
            return stats;
        });
    }

    @Override
    @Transactional
    public Mono<Void> blockUser(Long userId) {
        return userRepository.findById(userId)
            .flatMap(user -> {
                user.setBlocked(true);
                return userRepository.save(user)
                    .then(notificationService.createNotification(
                        userId,
                        "您的账号已被管理员封禁",
                        "SYSTEM"
                    ));
            })
            .then();
    }

    @Override
    @Transactional
    public Mono<Void> unblockUser(Long userId) {
        return userRepository.findById(userId)
            .flatMap(user -> {
                user.setBlocked(false);
                return userRepository.save(user)
                    .then(notificationService.createNotification(
                        userId,
                        "您的账号已被解除封禁",
                        "SYSTEM"
                    ));
            })
            .then();
    }

    @Override
    @Transactional
    public Mono<Void> verifyOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .flatMap(order -> {
                order.setVerified(true);
                return orderRepository.save(order)
                    .then(notificationService.createOrderNotification(
                        order.getInitiatorId(),
                        "您的订单已通过验证",
                        orderId
                    ));
            })
            .then();
    }

    @Override
    @Transactional
    public Mono<Void> cancelOrder(Long orderId, String reason) {
        return orderRepository.findById(orderId)
            .flatMap(order -> {
                order.setStatus(com.p2p.domain.Order.OrderStatus.CANCELLED);
                order.setCancelReason(reason);
                return orderRepository.save(order)
                    .then(notificationService.createOrderNotification(
                        order.getInitiatorId(),
                        "您的订单已被管理员取消：" + reason,
                        orderId
                    ));
            })
            .then();
    }

    @Override
    public Mono<Void> cleanupOldData(LocalDate before) {
        return Mono.when(
            orderRepository.deleteByCreatedAtBefore(before),
            paymentRepository.deleteByCreatedAtBefore(before)
        );
    }

    @Override
    public Mono<Void> refreshCache() {
        return redisTemplate.keys("*")
            .flatMap(redisTemplate.opsForValue()::delete)
            .then();
    }

    @Override
    public Mono<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return Mono.just(status);
    }

    @Override
    public Mono<Map<String, Object>> getDatabaseStatus() {
        return Mono.zip(
            userRepository.count(),
            orderRepository.count(),
            paymentRepository.count()
        ).map(tuple -> {
            Map<String, Object> status = new HashMap<>();
            status.put("users", tuple.getT1());
            status.put("orders", tuple.getT2());
            status.put("payments", tuple.getT3());
            return status;
        });
    }
} 