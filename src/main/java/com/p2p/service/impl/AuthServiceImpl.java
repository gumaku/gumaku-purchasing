package com.p2p.service.impl;

import com.p2p.domain.User;
import com.p2p.dto.auth.AuthResponse;
import com.p2p.dto.auth.LoginRequest;
import com.p2p.dto.auth.RegisterRequest;
import com.p2p.exception.UnauthorizedException;
import com.p2p.repository.UserRepository;
import com.p2p.security.JwtTokenProvider;
import com.p2p.service.AuthService;
import com.p2p.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final NotificationService notificationService;

    @Override
    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
            .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
            .flatMap(user -> {
                String accessToken = tokenProvider.generateToken(user);
                String refreshToken = tokenProvider.generateRefreshToken(user);
                return updateLastLoginTime(user.getId())
                    .thenReturn(new AuthResponse(accessToken, refreshToken, 
                        tokenProvider.getExpirationInSeconds()));
            });
    }

    @Override
    @Transactional
    public Mono<AuthResponse> register(RegisterRequest request) {
        return userRepository.findByEmail(request.getEmail())
            .flatMap(existingUser -> Mono.<User>error(
                new IllegalArgumentException("Email already registered")))
            .switchIfEmpty(Mono.defer(() -> {
                User newUser = new User();
                newUser.setEmail(request.getEmail());
                newUser.setUsername(request.getUsername());
                newUser.setPassword(passwordEncoder.encode(request.getPassword()));
                return userRepository.save(newUser);
            }))
            .flatMap(user -> {
                String accessToken = tokenProvider.generateToken(user);
                String refreshToken = tokenProvider.generateRefreshToken(user);
                return Mono.just(new AuthResponse(accessToken, refreshToken, 
                    tokenProvider.getExpirationInSeconds()));
            });
    }

    @Override
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        return Mono.just(refreshToken)
            .filter(tokenProvider::validateRefreshToken)
            .flatMap(token -> {
                String email = tokenProvider.getEmailFromToken(token);
                return userRepository.findByEmail(email)
                    .map(user -> {
                        String newAccessToken = tokenProvider.generateToken(user);
                        String newRefreshToken = tokenProvider.generateRefreshToken(user);
                        return new AuthResponse(newAccessToken, newRefreshToken, 
                            tokenProvider.getExpirationInSeconds());
                    });
            });
    }

    @Override
    @Transactional
    public Mono<Void> updateLastLoginTime(Long userId) {
        return userRepository.findById(userId)
            .flatMap(user -> {
                user.setLastLoginAt(ZonedDateTime.now());
                return userRepository.save(user);
            })
            .then();
    }

    @Override
    public Mono<User> getCurrentUser(String token) {
        return Mono.justOrEmpty(tokenProvider.getEmailFromToken(token))
            .flatMap(userRepository::findByEmail)
            .switchIfEmpty(Mono.error(new UnauthorizedException("User not found")))
            .filter(User::isEmailVerified)
            .switchIfEmpty(Mono.error(new UnauthorizedException("Email not verified")));
    }

    @Override
    public Mono<Boolean> isEmailVerified(String email) {
        return userRepository.findByEmail(email)
            .map(User::isEmailVerified)
            .switchIfEmpty(Mono.error(new UnauthorizedException()));
    }

    @Override
    public Mono<Void> sendVerificationEmail(String email) {
        return userRepository.findByEmail(email)
            .flatMap(user -> {
                String verificationToken = tokenProvider.createVerificationToken(email);
                // TODO: 發送驗證郵件
                return Mono.empty();
            });
    }

    @Override
    public Mono<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        return userRepository.findById(userId)
            .filter(user -> passwordEncoder.matches(oldPassword, user.getPassword()))
            .flatMap(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                return userRepository.save(user);
            })
            .doOnSuccess(user -> {
                if (user != null) {
                    notificationService.createNotification(
                        userId,
                        "您的密碼已成功更新",
                        "SECURITY"
                    ).subscribe();
                }
            })
            .then();
    }

    @Override
    public Mono<Void> resetPassword(String email) {
        return userRepository.findByEmail(email)
            .flatMap(user -> {
                String resetToken = tokenProvider.createPasswordResetToken(email);
                // TODO: 發送重置郵件
                return Mono.empty();
            });
    }

    @Override
    public Mono<Void> confirmPasswordReset(String token, String newPassword) {
        if (!tokenProvider.validateToken(token)) {
            return Mono.error(new UnauthorizedException("Invalid or expired token"));
        }

        String email = tokenProvider.getUsernameFromToken(token);
        return userRepository.findByEmail(email)
            .flatMap(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                return userRepository.save(user);
            })
            .then();
    }

    @Override
    public Mono<Void> logout(String refreshToken) {
        return Mono.just(refreshToken)
            .filter(tokenProvider::validateRefreshToken)
            .flatMap(token -> {
                String email = tokenProvider.getEmailFromToken(token);
                return userRepository.findByEmail(email)
                    .flatMap(user -> {
                        user.setLastLoginAt(ZonedDateTime.now());
                        return userRepository.save(user);
                    })
                    .then();
            });
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        try {
            String email = tokenProvider.getEmailFromToken(token);
            return userRepository.findByEmail(email)
                .map(user -> !user.isBlocked())
                .defaultIfEmpty(false);
        } catch (Exception e) {
            return Mono.just(false);
        }
    }
} 