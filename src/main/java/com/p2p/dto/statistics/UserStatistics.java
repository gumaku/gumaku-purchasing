package com.p2p.dto.statistics;

import lombok.Data;

import java.util.Map;

@Data
public class UserStatistics {
    private Long totalUsers;
    private Long activeUsers;
    private Map<String, Long> usersByMonth;
    private Map<String, Long> usersByRole;
    private Map<String, Double> creditScoreDistribution;
    private Map<String, Long> userActivityDistribution;
    private Double retentionRate;
} 