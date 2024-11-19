package com.p2p.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider tokenProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        
        try {
            if (!tokenProvider.validateToken(authToken)) {
                return Mono.empty();
            }

            String username = tokenProvider.getUsernameFromToken(authToken);
            var authorities = tokenProvider.getRolesFromToken(authToken).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
            );

            return Mono.just(auth);
        } catch (Exception e) {
            return Mono.empty();
        }
    }
} 