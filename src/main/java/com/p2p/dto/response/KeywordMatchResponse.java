package com.p2p.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeywordMatchResponse {
    private String keyword;
    private int matchCount;
    private List<String> matchedKeywords;
} 