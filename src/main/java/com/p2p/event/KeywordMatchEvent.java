package com.p2p.event;

import com.p2p.domain.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

@Getter
public class KeywordMatchEvent extends ApplicationEvent {
    private final Order order;
    private final Set<String> matchedKeywords;
    private final Set<Long> subscriberIds;

    public KeywordMatchEvent(Object source, Order order, Set<String> matchedKeywords, Set<Long> subscriberIds) {
        super(source);
        this.order = order;
        this.matchedKeywords = matchedKeywords;
        this.subscriberIds = subscriberIds;
    }
} 