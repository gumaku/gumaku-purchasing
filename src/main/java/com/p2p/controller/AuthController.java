package com.p2p.controller;

import com.p2p.domain.User;
import com.p2p.dto.auth.AuthRequest;
import com.p2p.dto.auth.AuthResponse;
import com.p2p.dto.auth.LoginRequest;
import com.p2p.dto.auth.RegisterRequest;
import com.p2p.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return authService.refreshToken(refreshToken)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<User>> getCurrentUser(
            @RequestHeader("Authorization") String token) {
        String bearerToken = token.substring(7); // Remove "Bearer " prefix
        return authService.getCurrentUser(bearerToken)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(
            @RequestHeader("Refresh-Token") String refreshToken) {
        return authService.logout(refreshToken)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping("/password/change")
    public Mono<ResponseEntity<Void>> changePassword(
            @AuthenticationPrincipal Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return authService.changePassword(userId, oldPassword, newPassword)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping("/password/reset/request")
    public Mono<ResponseEntity<Void>> requestPasswordReset(@RequestParam String email) {
        return authService.resetPassword(email)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @PostMapping("/password/reset/confirm")
    public Mono<ResponseEntity<Void>> confirmPasswordReset(
            @RequestParam String token,
            @RequestParam String newPassword) {
        return authService.confirmPasswordReset(token, newPassword)
            .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
} 