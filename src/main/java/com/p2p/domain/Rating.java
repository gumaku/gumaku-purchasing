package com.p2p.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Table("ratings")
public class Rating {
    @Id
    private Long id;
    
    private Long orderId;
    
    private Long raterId;
    
    private Long ratedUserId;
    
    private Integer score;
    
    private String comment;
    
    private RatingType type;
    
    private ZonedDateTime createdAt;
    
    @Version
    private Long version;

    public enum RatingType {
        INITIATOR_TO_PURCHASER,
        PURCHASER_TO_INITIATOR
    }

    public boolean isValidScore() {
        return score != null && score >= 1 && score <= 5;
    }

    public boolean canBeModified(Long userId) {
        return raterId.equals(userId) && 
               ZonedDateTime.now().minusHours(24).isBefore(createdAt);
    }

    public void preSave() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (!isValidScore()) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }
    }

    public static Rating createInitiatorRating(Long orderId, Long raterId, Long purchaserId) {
        Rating rating = new Rating();
        rating.setOrderId(orderId);
        rating.setRaterId(raterId);
        rating.setRatedUserId(purchaserId);
        rating.setType(RatingType.INITIATOR_TO_PURCHASER);
        return rating;
    }

    public static Rating createPurchaserRating(Long orderId, Long raterId, Long initiatorId) {
        Rating rating = new Rating();
        rating.setOrderId(orderId);
        rating.setRaterId(raterId);
        rating.setRatedUserId(initiatorId);
        rating.setType(RatingType.PURCHASER_TO_INITIATOR);
        return rating;
    }
} 