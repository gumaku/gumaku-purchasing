package com.p2p.controller;

import com.p2p.dto.statistics.OrderStatistics;
import com.p2p.dto.statistics.UserStatistics;
import com.p2p.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/statistics/orders")
    public Mono<ResponseEntity<OrderStatistics>> getOrderStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return adminService.getOrderStatistics(startDate, endDate)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/statistics/users")
    public Mono<ResponseEntity<UserStatistics>> getUserStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return adminService.getUserStatistics(startDate, endDate)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/system/status")
    public Mono<ResponseEntity<Map<String, Object>>> getSystemStatus() {
        return adminService.getSystemStatus()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/database/status")
    public Mono<ResponseEntity<Map<String, Object>>> getDatabaseStatus() {
        return adminService.getDatabaseStatus()
            .map(ResponseEntity::ok);
    }

    @PostMapping("/users/{userId}/block")
    public Mono<ResponseEntity<Void>> blockUser(@PathVariable Long userId) {
        return adminService.blockUser(userId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping("/users/{userId}/unblock")
    public Mono<ResponseEntity<Void>> unblockUser(@PathVariable Long userId) {
        return adminService.unblockUser(userId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping("/maintenance/cleanup")
    public Mono<ResponseEntity<Void>> cleanupOldData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate before) {
        return adminService.cleanupOldData(before)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping("/maintenance/cache/refresh")
    public Mono<ResponseEntity<Void>> refreshCache() {
        return adminService.refreshCache()
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
} 