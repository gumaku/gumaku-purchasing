package com.p2p.dto.response;

import com.p2p.domain.KeywordSubscription;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class KeywordSubscriptionResponse {
    private Long id;
    private Long userId;
    private String keyword;
    private ZonedDateTime createdAt;
    private UserResponse user; // 訂閱者信息

    public static KeywordSubscriptionResponse fromKeywordSubscription(KeywordSubscription subscription) {
        KeywordSubscriptionResponse response = new KeywordSubscriptionResponse();
        response.setId(subscription.getId());
        response.setUserId(subscription.getUserId());
        response.setKeyword(subscription.getKeyword());
        response.setCreatedAt(subscription.getCreatedAt());
        return response;
    }
} 