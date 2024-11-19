package com.p2p.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Table("users")
public class User {
    @Id
    private Long id;
    private String email;
    private String username;
    private String password;
    private String avatar;
    private String phone;
    private Set<Role> roles;
    @Transient
    private Set<String> subscribedKeywords;
    private boolean emailVerified;
    private boolean phoneVerified;
    private ZonedDateTime lastLoginAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private NotificationSettings notificationSettings;
    private boolean blocked;
    private Double creditScore;

    public enum Role {
        INITIATOR,
        PURCHASER,
        ADMIN,
        MODERATOR
    }

    @Data
    public static class NotificationSettings {
        private boolean emailEnabled;
        private boolean pushEnabled;
        private boolean orderUpdates;
        private boolean paymentUpdates;
        private boolean marketingEmails;
    }

    public void subscribeToKeyword(String keyword) {
        if (subscribedKeywords == null) {
            subscribedKeywords = new HashSet<>();
        }
        subscribedKeywords.add(keyword.toLowerCase());
        this.updatedAt = ZonedDateTime.now();
    }

    public void unsubscribeFromKeyword(String keyword) {
        if (subscribedKeywords != null) {
            subscribedKeywords.remove(keyword.toLowerCase());
            this.updatedAt = ZonedDateTime.now();
        }
    }

    public Set<String> getSubscribedKeywords() {
        if (subscribedKeywords == null) {
            subscribedKeywords = new HashSet<>();
        }
        return subscribedKeywords;
    }

    public void setSubscribedKeywords(Set<String> keywords) {
        this.subscribedKeywords = keywords != null ? keywords : new HashSet<>();
    }

    public void updateLastLoginTime() {
        this.lastLoginAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    public void updateCreditScore(Double score) {
        this.creditScore = score;
        this.updatedAt = ZonedDateTime.now();
    }
} 