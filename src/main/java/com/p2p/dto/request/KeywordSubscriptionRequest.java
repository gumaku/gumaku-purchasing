package com.p2p.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class KeywordSubscriptionRequest {
    @NotBlank(message = "Keyword is required")
    @Size(min = 2, max = 50, message = "Keyword must be between 2 and 50 characters")
    private String keyword;
} 