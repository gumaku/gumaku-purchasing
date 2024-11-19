package com.p2p.listener;

import com.p2p.domain.Order;
import com.p2p.event.KeywordMatchEvent;
import com.p2p.repository.KeywordSubscriptionRepository;
import com.p2p.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKeywordListener {

    private final KeywordSubscriptionRepository keywordSubscriptionRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    public void handleOrderCreated(Order order) {
        log.info("Checking keywords for order: {}", order.getId());
        
        // 獲取訂單相關的文本
        String searchText = String.join(" ", 
            order.getProductName(),
            order.getDescription(),
            order.getDeliveryCountry()
        ).toLowerCase();

        // 查找匹配的關鍵字訂閱
        keywordSubscriptionRepository.findMatchingSubscriptions(searchText)
            .collectList()
            .flatMap(subscriptions -> {
                if (subscriptions.isEmpty()) {
                    return Mono.empty();
                }

                Set<String> matchedKeywords = new HashSet<>();
                Set<Long> subscriberIds = new HashSet<>();

                subscriptions.forEach(subscription -> {
                    matchedKeywords.add(subscription.getKeyword());
                    subscriberIds.add(subscription.getUserId());
                });

                // 發布關鍵字匹配事件
                eventPublisher.publishEvent(new KeywordMatchEvent(
                    this, order, matchedKeywords, subscriberIds
                ));

                // 向所有訂閱者發送通知
                return Mono.when(
                    subscriberIds.stream()
                        .map(userId -> notificationService.createNotification(
                            userId,
                            String.format(
                                "New order matching your keywords: %s",
                                String.join(", ", matchedKeywords)
                            ),
                            "KEYWORD_MATCH"
                        ))
                        .toArray(Mono[]::new)
                );
            })
            .subscribe(
                null,
                error -> log.error("Error processing keyword matches", error)
            );
    }

    @Async
    @EventListener
    public void handleKeywordMatch(KeywordMatchEvent event) {
        log.info("Processing keyword match event for order: {}", event.getOrder().getId());
        
        // 更新關鍵字匹配統計
        keywordSubscriptionRepository.incrementMatchCount(event.getMatchedKeywords())
            .subscribe(
                null,
                error -> log.error("Error updating keyword match statistics", error)
            );
    }
} 