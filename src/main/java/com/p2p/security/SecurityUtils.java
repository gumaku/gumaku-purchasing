package com.p2p.security;

import com.p2p.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

public class SecurityUtils {

    private SecurityUtils() {
        // 私有構造函數防止實例化
    }

    public static Mono<String> getCurrentUserEmail() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName)
            .switchIfEmpty(Mono.error(() -> 
                new UnauthorizedException("No authenticated user found")));
    }

    public static Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(auth -> {
                if (auth.getPrincipal() instanceof Long) {
                    return (Long) auth.getPrincipal();
                }
                throw new UnauthorizedException("Invalid user principal type");
            })
            .switchIfEmpty(Mono.error(() -> 
                new UnauthorizedException("No authenticated user found")));
    }

    public static Mono<Boolean> isCurrentUser(Long userId) {
        return getCurrentUserId()
            .map(currentUserId -> currentUserId.equals(userId))
            .onErrorResume(UnauthorizedException.class, e -> Mono.just(false));
    }

    public static Mono<Boolean> hasRole(String role) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(auth -> auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role)))
            .defaultIfEmpty(false);
    }

    public static Mono<Boolean> hasAnyRole(String... roles) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(auth -> {
                var authorities = auth.getAuthorities();
                for (String role : roles) {
                    if (authorities.stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role))) {
                        return true;
                    }
                }
                return false;
            })
            .defaultIfEmpty(false);
    }
} 