package com.p2p.service;

import com.p2p.domain.Order;
import com.p2p.dto.order.OrderRequest;
import com.p2p.dto.order.OrderSearchRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface OrderService {
    // 訂單基本操作
    Mono<Order> createOrder(Long userId, OrderRequest request);
    Mono<Order> findById(Long id);
    Flux<Order> findByInitiatorId(Long initiatorId, int page, int size);
    Flux<Order> findByPurchaserId(Long purchaserId, int page, int size);
    Flux<Order> findByStatus(Order.OrderStatus status);
    
    // 訂單狀態管理
    Mono<Order> acceptOrder(Long orderId, Long purchaserId);
    Mono<Order> cancelOrder(Long orderId, Long userId);
    Mono<Order> completeOrder(Long orderId, Long userId);
    Mono<Order> updateOrderAfterPayment(Long orderId);
    
    // 團購相關
    Mono<Order> joinGroupOrder(Long orderId, Long userId, Integer quantity);
    
    // 訂單搜索
    Flux<Order> searchOrders(OrderSearchRequest request);
    
    // 訂單統計
    Mono<Long> countByStatus(Order.OrderStatus status);
    Mono<BigDecimal> calculateAverageOrderAmount();
    Flux<Order> findRecentOrders(int limit);
} 