package com.p2p.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KeywordStatisticsResponse {
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Map<String, Long> subscriptionsByKeyword;
    private Map<String, Long> matchesByKeyword;
    private List<String> trendingKeywords;
    private Map<String, List<String>> relatedKeywords;
    private Map<String, Long> subscriptionsByUser;
    private Map<String, Double> matchRateByKeyword;
} 