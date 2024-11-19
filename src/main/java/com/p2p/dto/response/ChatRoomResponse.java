package com.p2p.dto.response;

import com.p2p.domain.ChatRoom;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
public class ChatRoomResponse {
    private Long id;
    private String name;
    private Long orderId;
    private ChatRoom.RoomType type;
    private Set<Long> memberIds;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastActivityAt;
    private OrderResponse order; // 關聯的訂單信息
    private Long unreadCount; // 未讀消息數量

    public static ChatRoomResponse fromChatRoom(ChatRoom chatRoom) {
        ChatRoomResponse response = new ChatRoomResponse();
        response.setId(chatRoom.getId());
        response.setName(chatRoom.getName());
        response.setOrderId(chatRoom.getOrderId());
        response.setType(chatRoom.getType());
        response.setMemberIds(chatRoom.getMemberIds());
        response.setCreatedAt(chatRoom.getCreatedAt());
        response.setLastActivityAt(chatRoom.getLastActivityAt());
        return response;
    }
} 