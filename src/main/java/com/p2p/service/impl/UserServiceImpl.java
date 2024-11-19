package com.p2p.service.impl;

import com.p2p.domain.User;
import com.p2p.repository.UserRepository;
import com.p2p.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public Mono<User> save(User user) {
        if (user.getId() == null) {
            user.setCreatedAt(ZonedDateTime.now());
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles(Collections.singleton(User.Role.INITIATOR));
            }
        }
        user.setUpdatedAt(ZonedDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public Mono<User> updateCreditScore(Long userId, Double score) {
        return userRepository.findById(userId)
            .flatMap(user -> {
                user.updateCreditScore(score);
                return userRepository.save(user);
            });
    }

    @Override
    @Transactional
    public Mono<User> addKeywordSubscription(Long userId, String keyword) {
        return userRepository.findById(userId)
            .flatMap(user -> {
                user.subscribeToKeyword(keyword);
                return userRepository.save(user);
            });
    }

    @Override
    @Transactional
    public Mono<User> removeKeywordSubscription(Long userId, String keyword) {
        return userRepository.findById(userId)
            .flatMap(user -> {
                user.unsubscribeFromKeyword(keyword);
                return userRepository.save(user);
            });
    }

    @Override
    @Transactional
    public Mono<User> updateNotificationSettings(Long userId, User.NotificationSettings settings) {
        return userRepository.findById(userId)
            .flatMap(user -> {
                user.setNotificationSettings(settings);
                return userRepository.save(user);
            });
    }

    @Override
    @Transactional
    public Mono<Void> updateLastLoginTime(Long userId) {
        return userRepository.findById(userId)
            .flatMap(user -> {
                user.updateLastLoginTime();
                return userRepository.save(user);
            })
            .then();
    }

    @Override
    public Mono<Long> countUsersByRole(User.Role role) {
        return userRepository.countByRolesContaining(role.name());
    }
} 