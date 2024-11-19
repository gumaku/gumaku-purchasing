package com.p2p.dto.response;

import com.p2p.domain.User;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String avatar;
    private String phone;
    private Set<String> roles;
    private Set<String> subscribedKeywords;
    private boolean emailVerified;
    private boolean phoneVerified;
    private Double creditScore;
    private ZonedDateTime lastLoginAt;
    private ZonedDateTime createdAt;

    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setPhone(user.getPhone());
        response.setRoles(user.getRoles().stream()
            .map(Enum::name)
            .collect(java.util.stream.Collectors.toSet()));
        response.setSubscribedKeywords(user.getSubscribedKeywords());
        response.setEmailVerified(user.isEmailVerified());
        response.setPhoneVerified(user.isPhoneVerified());
        response.setCreditScore(user.getCreditScore());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
} 