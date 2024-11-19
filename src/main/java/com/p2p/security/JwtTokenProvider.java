package com.p2p.security;

import com.p2p.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${security.jwt.verification-expiration}")
    private long verificationExpiration;

    @Value("${security.jwt.reset-expiration}")
    private long resetExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user) {
        return generateToken(user.getEmail(), user.getRoles(), jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user.getEmail(), user.getRoles(), refreshExpiration);
    }

    public String createVerificationToken(String email) {
        return generateToken(email, Set.of(), verificationExpiration);
    }

    public String createPasswordResetToken(String email) {
        return generateToken(email, Set.of(), resetExpiration);
    }

    private String generateToken(String subject, Set<User.Role> roles, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .setSubject(subject)
            .claim("roles", roles.stream()
                .map(Enum::name)
                .collect(Collectors.toList()))
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims.getSubject();
    }

    public String getUsernameFromToken(String token) {
        return getEmailFromToken(token);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return ((java.util.List<String>) claims.get("roles", java.util.List.class))
            .stream()
            .collect(Collectors.toSet());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }

    public long getExpirationInSeconds() {
        return jwtExpiration / 1000;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
} 