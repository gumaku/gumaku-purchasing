package com.p2p.dto.response;

import com.p2p.domain.ChatMessage;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ChatMessageResponse {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private ChatMessage.MessageType type;
    private ZonedDateTime createdAt;
    
    // 關聯信息
    private UserResponse sender;
    private ChatRoomResponse chatRoom;

    public static ChatMessageResponse fromChatMessage(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setChatRoomId(message.getChatRoomId());
        response.setSenderId(message.getSenderId());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
} 