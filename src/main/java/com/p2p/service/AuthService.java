package com.p2p.service;

import com.p2p.domain.User;
import com.p2p.dto.auth.AuthResponse;
import com.p2p.dto.auth.LoginRequest;
import com.p2p.dto.auth.RegisterRequest;
import reactor.core.publisher.Mono;

public interface AuthService {
    // 用戶認證
    Mono<AuthResponse> login(LoginRequest request);
    Mono<AuthResponse> register(RegisterRequest request);
    Mono<AuthResponse> refreshToken(String refreshToken);
    
    // 密碼管理
    Mono<Void> changePassword(Long userId, String oldPassword, String newPassword);
    Mono<Void> resetPassword(String email);
    Mono<Void> confirmPasswordReset(String token, String newPassword);
    
    // 會話管理
    Mono<Void> logout(String refreshToken);
    Mono<Void> updateLastLoginTime(Long userId);
    Mono<Boolean> validateToken(String token);
    
    // 用戶驗證
    Mono<User> getCurrentUser(String token);
    Mono<Boolean> isEmailVerified(String email);
    Mono<Void> sendVerificationEmail(String email);
} 