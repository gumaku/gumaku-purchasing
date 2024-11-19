package com.p2p.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/docs")
public class ApiDocController {

    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> getApiInfo() {
        return Mono.just(ResponseEntity.ok(Map.of(
            "version", "1.0",
            "baseUrl", "/api",
            "endpoints", Map.of(
                "auth", Map.of(
                    "login", "/api/auth/login",
                    "register", "/api/auth/register"
                ),
                "orders", Map.of(
                    "create", "/api/orders",
                    "search", "/api/orders/search"
                ),
                "payments", Map.of(
                    "create", "/api/payments/create-intent",
                    "confirm", "/api/payments/confirm"
                ),
                "users", Map.of(
                    "profile", "/api/users/me",
                    "settings", "/api/users/me/settings"
                )
            ),
            "websockets", Map.of(
                "chat", "/ws/chat/{roomId}",
                "notifications", "/ws/notifications/{userId}"
            )
        )));
    }
} 