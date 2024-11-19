package com.p2p.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AuthRequest {
    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "請輸入有效的電子郵件地址")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 50, message = "密碼長度必須在6到50個字符之間")
    private String password;

    private boolean rememberMe = false;
} 