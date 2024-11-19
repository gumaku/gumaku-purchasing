package com.p2p.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Table("keyword_subscriptions")
public class KeywordSubscription {
    @Id
    private Long id;
    private Long userId;
    private String keyword;
    private Long matchCount;
    private ZonedDateTime lastMatchAt;
    private ZonedDateTime createdAt;
    @Version
    private Long version;

    public void incrementMatchCount() {
        if (matchCount == null) {
            matchCount = 1L;
        } else {
            matchCount++;
        }
        lastMatchAt = ZonedDateTime.now();
    }

    public void preSave() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (matchCount == null) {
            matchCount = 0L;
        }
    }

    public boolean isActive() {
        if (lastMatchAt == null) {
            return true;
        }
        return ZonedDateTime.now().minusDays(30).isBefore(lastMatchAt);
    }

    public boolean hasMatches() {
        return matchCount != null && matchCount > 0;
    }
} 