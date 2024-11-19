package com.p2p.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class UserUpdateRequest {
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    private String avatar;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
} 