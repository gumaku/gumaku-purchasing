package com.p2p.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TokenRefreshRequest {
    @NotBlank(message = "刷新令牌不能為空")
    private String refreshToken;
} 