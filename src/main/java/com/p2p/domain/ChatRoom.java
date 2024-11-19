package com.p2p.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Table("chat_rooms")
public class ChatRoom {
    @Id
    private Long id;
    private String name;
    private Long orderId;
    private RoomType type;
    private Set<Long> memberIds;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastActivityAt;
    @Version
    private Long version;

    public enum RoomType {
        ORDER,
        GROUP,
        SUPPORT
    }

    public void addMember(Long userId) {
        if (memberIds == null) {
            memberIds = new HashSet<>();
        }
        memberIds.add(userId);
        updateLastActivity();
    }

    public void removeMember(Long userId) {
        if (memberIds != null) {
            memberIds.remove(userId);
            updateLastActivity();
        }
    }

    public boolean isMember(Long userId) {
        return memberIds != null && memberIds.contains(userId);
    }

    public void updateLastActivity() {
        this.lastActivityAt = ZonedDateTime.now();
    }

    public boolean isActive() {
        return lastActivityAt != null && 
               ZonedDateTime.now().minusDays(7).isBefore(lastActivityAt);
    }

    public boolean isOrderRoom() {
        return type == RoomType.ORDER && orderId != null;
    }

    public int getMemberCount() {
        return memberIds != null ? memberIds.size() : 0;
    }

    public void preSave() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
            lastActivityAt = createdAt;
        }
        if (memberIds == null) {
            memberIds = new HashSet<>();
        }
    }
} 