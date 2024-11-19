package com.p2p.service.impl;

import com.p2p.domain.Order;
import com.p2p.dto.order.OrderRequest;
import com.p2p.dto.order.OrderSearchRequest;
import com.p2p.event.OrderEvent;
import com.p2p.repository.OrderRepository;
import com.p2p.service.NotificationService;
import com.p2p.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Mono<Order> createOrder(Long userId, OrderRequest request) {
        Order order = new Order();
        order.setInitiatorId(userId);
        order.setProductName(request.getProductName());
        order.setProductPrice(request.getProductPrice());
        order.setQuantity(request.getQuantity());
        order.setDescription(request.getDescription());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setDeliveryCountry(request.getDeliveryCountry());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(ZonedDateTime.now());

        return orderRepository.save(order)
            .doOnSuccess(savedOrder -> {
                eventPublisher.publishEvent(
                    new OrderEvent(this, savedOrder, OrderEvent.OrderEventType.CREATED)
                );
            });
    }

    @Override
    public Mono<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Flux<Order> findByInitiatorId(Long initiatorId, int page, int size) {
        return orderRepository.findByInitiatorId(initiatorId)
            .skip(page * size)
            .take(size);
    }

    @Override
    public Flux<Order> findByPurchaserId(Long purchaserId, int page, int size) {
        return orderRepository.findByPurchaserId(purchaserId)
            .skip(page * size)
            .take(size);
    }

    @Override
    public Flux<Order> findByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Mono<Order> acceptOrder(Long orderId, Long purchaserId) {
        return orderRepository.findById(orderId)
            .filter(order -> order.getStatus() == Order.OrderStatus.PENDING)
            .flatMap(order -> {
                order.setStatus(Order.OrderStatus.ACCEPTED);
                order.setPurchaserId(purchaserId);
                order.setUpdatedAt(ZonedDateTime.now());
                return orderRepository.save(order)
                    .doOnSuccess(savedOrder -> {
                        eventPublisher.publishEvent(
                            new OrderEvent(this, savedOrder, OrderEvent.OrderEventType.ACCEPTED)
                        );
                    });
            });
    }

    @Override
    @Transactional
    public Mono<Order> cancelOrder(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
            .filter(order -> order.getStatus() != Order.OrderStatus.COMPLETED)
            .filter(order -> order.getInitiatorId().equals(userId) || 
                           (order.getPurchaserId() != null && order.getPurchaserId().equals(userId)))
            .flatMap(order -> {
                order.setStatus(Order.OrderStatus.CANCELLED);
                order.setUpdatedAt(ZonedDateTime.now());
                return orderRepository.save(order)
                    .doOnSuccess(savedOrder -> {
                        eventPublisher.publishEvent(
                            new OrderEvent(this, savedOrder, OrderEvent.OrderEventType.CANCELLED)
                        );
                    });
            });
    }

    @Override
    @Transactional
    public Mono<Order> completeOrder(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
            .filter(order -> order.getStatus() == Order.OrderStatus.ACCEPTED)
            .filter(order -> order.getInitiatorId().equals(userId) || 
                           order.getPurchaserId().equals(userId))
            .flatMap(order -> {
                order.setStatus(Order.OrderStatus.COMPLETED);
                order.setUpdatedAt(ZonedDateTime.now());
                return orderRepository.save(order)
                    .doOnSuccess(savedOrder -> {
                        eventPublisher.publishEvent(
                            new OrderEvent(this, savedOrder, OrderEvent.OrderEventType.COMPLETED)
                        );
                    });
            });
    }

    @Override
    @Transactional
    public Mono<Order> joinGroupOrder(Long orderId, Long userId, Integer quantity) {
        return orderRepository.findById(orderId)
            .filter(order -> order.getStatus() == Order.OrderStatus.PENDING)
            .flatMap(order -> {
                order.addParticipant(userId, quantity);
                return orderRepository.save(order);
            });
    }

    @Override
    public Flux<Order> searchOrders(OrderSearchRequest request) {
        return orderRepository.searchOrders(
            request.getKeyword(),
            request.getStatus(),
            request.getMinPrice().doubleValue(),
            request.getMaxPrice().doubleValue()
        ).skip(request.getPage() * request.getSize())
         .take(request.getSize());
    }

    @Override
    public Mono<Long> countByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public Mono<BigDecimal> calculateAverageOrderAmount() {
        return orderRepository.calculateAverageOrderAmount()
            .defaultIfEmpty(BigDecimal.ZERO);
    }

    @Override
    public Flux<Order> findRecentOrders(int limit) {
        return orderRepository.findByOrderByCreatedAtDesc()
            .take(limit);
    }

    @Override
    @Transactional
    public Mono<Order> updateOrderAfterPayment(Long orderId) {
        return orderRepository.findById(orderId)
            .filter(order -> order.getStatus() == Order.OrderStatus.ACCEPTED)
            .flatMap(order -> {
                order.setStatus(Order.OrderStatus.IN_PROGRESS);
                order.setUpdatedAt(ZonedDateTime.now());
                return orderRepository.save(order)
                    .doOnSuccess(savedOrder -> {
                        eventPublisher.publishEvent(
                            new OrderEvent(this, savedOrder, OrderEvent.OrderEventType.PAYMENT_RECEIVED)
                        );
                        notificationService.createOrderNotification(
                            savedOrder.getInitiatorId(),
                            "訂單付款已確認，開始處理",
                            savedOrder.getId()
                        ).subscribe();
                        if (savedOrder.getPurchaserId() != null) {
                            notificationService.createOrderNotification(
                                savedOrder.getPurchaserId(),
                                "訂單付款已確認，開始處理",
                                savedOrder.getId()
                            ).subscribe();
                        }
                    });
            });
    }
} 