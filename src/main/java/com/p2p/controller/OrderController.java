package com.p2p.controller;

import com.p2p.domain.Order;
import com.p2p.dto.order.OrderRequest;
import com.p2p.dto.order.OrderSearchRequest;
import com.p2p.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<ResponseEntity<Order>> createOrder(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(userId, request)
            .map(ResponseEntity::ok)
            .doOnSuccess(order -> log.info("Order created by user: {}", userId));
    }

    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<Order>> getOrder(@PathVariable Long orderId) {
        return orderService.findById(orderId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-orders")
    public Flux<Order> getMyOrders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.findByInitiatorId(userId, page, size);
    }

    @GetMapping("/purchased")
    public Flux<Order> getPurchasedOrders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.findByPurchaserId(userId, page, size);
    }

    @PostMapping("/{orderId}/accept")
    public Mono<ResponseEntity<Order>> acceptOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId) {
        return orderService.acceptOrder(orderId, userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/complete")
    public Mono<ResponseEntity<Order>> completeOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId) {
        return orderService.completeOrder(orderId, userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/cancel")
    public Mono<ResponseEntity<Order>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId) {
        return orderService.cancelOrder(orderId, userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{orderId}/join")
    public Mono<ResponseEntity<Order>> joinGroupOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId,
            @RequestParam Integer quantity) {
        return orderService.joinGroupOrder(orderId, userId, quantity)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/search")
    public Flux<Order> searchOrders(@RequestBody OrderSearchRequest request) {
        return orderService.searchOrders(request);
    }

    @GetMapping("/recent")
    public Flux<Order> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return orderService.findRecentOrders(limit);
    }
} 