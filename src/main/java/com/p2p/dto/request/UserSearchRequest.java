package com.p2p.dto.request;

import lombok.Data;
import com.p2p.domain.User;

@Data
public class UserSearchRequest {
    private String keyword;
    private User.Role role;
    private Double minCreditScore;
    private Double maxCreditScore;
    private Boolean blocked;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    private Integer page = 0;
    private Integer size = 20;
} 