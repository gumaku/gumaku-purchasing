package com.p2p.service;

import com.p2p.domain.User;
import reactor.core.publisher.Mono;

public interface UserService {
    // 基本操作
    Mono<User> findById(Long id);
    Mono<User> findByEmail(String email);
    Mono<User> save(User user);
    
    // 信用評分
    Mono<User> updateCreditScore(Long userId, Double score);
    
    // 關鍵字訂閱
    Mono<User> addKeywordSubscription(Long userId, String keyword);
    Mono<User> removeKeywordSubscription(Long userId, String keyword);
    
    // 通知設置
    Mono<User> updateNotificationSettings(Long userId, User.NotificationSettings settings);
    
    // 登入時間
    Mono<Void> updateLastLoginTime(Long userId);
    
    // 統計
    Mono<Long> countUsersByRole(User.Role role);
} 