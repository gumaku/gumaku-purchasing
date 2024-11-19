package com.p2p.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class ChatStatisticsResponse {
    private Long totalMessages;
    private Long activeRooms;
    private Map<String, Long> messagesByType;
    private Map<String, Long> messagesByHour;
    private Map<String, Long> activeUsers;
    private Double averageResponseTime;
    private Map<Long, Long> messagesByRoom;
    private Map<String, Long> messagesByDate;
} 