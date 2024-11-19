package com.p2p.controller;

import com.p2p.domain.User;
import com.p2p.dto.user.NotificationSettingsRequest;
import com.p2p.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Mono<ResponseEntity<User>> getCurrentUser(
            @AuthenticationPrincipal Long userId) {
        return userService.findById(userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<User>> getUser(@PathVariable Long userId) {
        return userService.findById(userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/notification-settings")
    public Mono<ResponseEntity<User>> updateNotificationSettings(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NotificationSettingsRequest request) {
        return userService.updateNotificationSettings(userId, request.toSettings())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/notification-settings")
    public Mono<ResponseEntity<User.NotificationSettings>> getNotificationSettings(
            @AuthenticationPrincipal Long userId) {
        return userService.findById(userId)
            .map(User::getNotificationSettings)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/credit-score")
    public Mono<ResponseEntity<User>> updateCreditScore(
            @AuthenticationPrincipal Long userId,
            @RequestParam Double score) {
        return userService.updateCreditScore(userId, score)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/me/keywords/{keyword}")
    public Mono<ResponseEntity<User>> addKeywordSubscription(
            @AuthenticationPrincipal Long userId,
            @PathVariable String keyword) {
        return userService.addKeywordSubscription(userId, keyword)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/me/keywords/{keyword}")
    public Mono<ResponseEntity<User>> removeKeywordSubscription(
            @AuthenticationPrincipal Long userId,
            @PathVariable String keyword) {
        return userService.removeKeywordSubscription(userId, keyword)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
} 