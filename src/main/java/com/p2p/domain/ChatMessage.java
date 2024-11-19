package com.p2p.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Table("chat_messages")
public class ChatMessage {
    @Id
    private Long id;
    @Column("chat_room_id")
    private Long chatRoomId;
    @Column("sender_id")
    private Long senderId;
    private String content;
    @Column("message_type")
    private MessageType type;
    @Column("created_at")
    private ZonedDateTime createdAt;
    @Column("updated_at")
    private ZonedDateTime updatedAt;

    public enum MessageType {
        TEXT,
        IMAGE,
        SYSTEM,
        ORDER_UPDATE
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = ZonedDateTime.now();
    }

    public static ChatMessage createSystemMessage(Long chatRoomId, String content) {
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setContent(content);
        message.setType(MessageType.SYSTEM);
        message.setCreatedAt(ZonedDateTime.now());
        message.setUpdatedAt(ZonedDateTime.now());
        return message;
    }

    public static ChatMessage createOrderUpdate(Long chatRoomId, String content) {
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setContent(content);
        message.setType(MessageType.ORDER_UPDATE);
        message.setCreatedAt(ZonedDateTime.now());
        message.setUpdatedAt(ZonedDateTime.now());
        return message;
    }

    public boolean isSystemMessage() {
        return type == MessageType.SYSTEM || type == MessageType.ORDER_UPDATE;
    }

    public boolean canBeDeleted(Long userId) {
        return !isSystemMessage() && senderId.equals(userId);
    }

    public boolean isRecent() {
        return ZonedDateTime.now().minusHours(24).isBefore(createdAt);
    }
} 