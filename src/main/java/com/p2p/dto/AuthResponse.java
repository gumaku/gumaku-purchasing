package com.p2p.dto;

import com.p2p.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String username;
        private String avatar;
        private Set<String> roles;
        private boolean emailVerified;
        private Double creditScore;

        public static UserInfo fromUser(User user) {
            return new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getAvatar(),
                user.getRoles().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet()),
                user.isEmailVerified(),
                user.getCreditScore()
            );
        }
    }

    public static AuthResponse from(String accessToken, String refreshToken, long expiresIn, User user) {
        return new AuthResponse(
            accessToken,
            refreshToken,
            expiresIn,
            UserInfo.fromUser(user)
        );
    }
} 