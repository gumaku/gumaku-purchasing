package com.p2p.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthRequest {
    @NotBlank(message = "電子郵件不能為空")
    private String email;

    @NotBlank(message = "密碼不能為空")
    private String password;
} 