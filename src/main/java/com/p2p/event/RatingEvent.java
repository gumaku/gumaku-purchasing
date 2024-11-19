package com.p2p.event;

import com.p2p.domain.Rating;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RatingEvent extends ApplicationEvent {
    private final Rating rating;
    private final RatingEventType type;

    public RatingEvent(Object source, Rating rating, RatingEventType type) {
        super(source);
        this.rating = rating;
        this.type = type;
    }

    public enum RatingEventType {
        CREATED,
        UPDATED,
        DELETED
    }
} 