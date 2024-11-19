package com.p2p.service.impl;

import com.p2p.dto.statistics.OrderStatistics;
import com.p2p.dto.statistics.UserStatistics;
import com.p2p.repository.*;
import com.p2p.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

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
    public Mono<Map<String, Long>> getOrderCountByStatus() {
        return orderRepository.countOrdersByStatus();
    }

    @Override
    public Mono<Map<String, Long>> getOrderCountByMonth() {
        LocalDate startDate = LocalDate.now().minusMonths(11);
        LocalDate endDate = LocalDate.now();
        return orderRepository.countOrdersByMonth(startDate, endDate);
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
    public Mono<Map<String, Long>> getUserCountByRole() {
        return userRepository.countUsersByRole();
    }

    @Override
    public Mono<Map<String, Long>> getActiveUsersByDay() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();
        return userRepository.countActiveUsersByDay(startDate, endDate);
    }

    @Override
    public Mono<Map<String, Long>> getActiveUsersByHour() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        return userRepository.calculateUserActivityDistributionBetween(startDate, endDate);
    }

    @Override
    public Mono<Map<String, Double>> getUserCreditScoreDistribution() {
        return userRepository.calculateCreditScoreDistribution();
    }

    @Override
    public Mono<Map<String, Object>> getTransactionStatistics(LocalDate startDate, LocalDate endDate) {
        return Mono.zip(
            orderRepository.calculateTotalTransactionAmount(startDate, endDate),
            paymentRepository.countSuccessfulPayments(),
            paymentRepository.calculateAverageAmount()
        ).map(tuple -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAmount", tuple.getT1());
            stats.put("successfulPayments", tuple.getT2());
            stats.put("averageAmount", tuple.getT3());
            return stats;
        });
    }

    @Override
    public Mono<Map<String, Object>> getRevenueByPeriod(String period) {
        LocalDate startDate = switch (period.toLowerCase()) {
            case "day" -> LocalDate.now().minusDays(1);
            case "week" -> LocalDate.now().minusWeeks(1);
            case "month" -> LocalDate.now().minusMonths(1);
            case "year" -> LocalDate.now().minusYears(1);
            default -> LocalDate.now().minusMonths(1);
        };
        return getTransactionStatistics(startDate, LocalDate.now());
    }

    @Override
    public Mono<byte[]> generateOrderReport(LocalDate startDate, LocalDate endDate) {
        // 实现在之前的代码中
        return Mono.empty();
    }

    @Override
    public Mono<byte[]> generateUserReport(LocalDate startDate, LocalDate endDate) {
        // 实现在之前的代码中
        return Mono.empty();
    }

    @Override
    public Mono<byte[]> generateFinancialReport(LocalDate startDate, LocalDate endDate) {
        // 实现在之前的代码中
        return Mono.empty();
    }
} 