package com.p2p.controller;

import com.p2p.dto.statistics.OrderStatistics;
import com.p2p.dto.statistics.UserStatistics;
import com.p2p.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/statistics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/orders")
    public Mono<ResponseEntity<OrderStatistics>> getOrderStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getOrderStatistics(startDate, endDate)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/orders/status")
    public Mono<ResponseEntity<Map<String, Long>>> getOrderCountByStatus() {
        return statisticsService.getOrderCountByStatus()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/orders/monthly")
    public Mono<ResponseEntity<Map<String, Long>>> getOrderCountByMonth() {
        return statisticsService.getOrderCountByMonth()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/users")
    public Mono<ResponseEntity<UserStatistics>> getUserStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getUserStatistics(startDate, endDate)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/users/roles")
    public Mono<ResponseEntity<Map<String, Long>>> getUserCountByRole() {
        return statisticsService.getUserCountByRole()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/users/active/daily")
    public Mono<ResponseEntity<Map<String, Long>>> getActiveUsersByDay() {
        return statisticsService.getActiveUsersByDay()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/users/active/hourly")
    public Mono<ResponseEntity<Map<String, Long>>> getActiveUsersByHour() {
        return statisticsService.getActiveUsersByHour()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/users/credit-score")
    public Mono<ResponseEntity<Map<String, Double>>> getUserCreditScoreDistribution() {
        return statisticsService.getUserCreditScoreDistribution()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/transactions")
    public Mono<ResponseEntity<Map<String, Object>>> getTransactionStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.getTransactionStatistics(startDate, endDate)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/revenue/{period}")
    public Mono<ResponseEntity<Map<String, Object>>> getRevenueByPeriod(
            @PathVariable String period) {
        return statisticsService.getRevenueByPeriod(period)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/reports/orders")
    public Mono<ResponseEntity<byte[]>> downloadOrderReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.generateOrderReport(startDate, endDate)
            .map(bytes -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order-report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes));
    }

    @GetMapping("/reports/users")
    public Mono<ResponseEntity<byte[]>> downloadUserReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.generateUserReport(startDate, endDate)
            .map(bytes -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes));
    }

    @GetMapping("/reports/financial")
    public Mono<ResponseEntity<byte[]>> downloadFinancialReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statisticsService.generateFinancialReport(startDate, endDate)
            .map(bytes -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=financial-report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes));
    }
} 