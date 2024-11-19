package com.p2p.dto.response;

import com.p2p.domain.Rating;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class RatingResponse {
    private Long id;
    private Long orderId;
    private Long raterId;
    private Long ratedUserId;
    private Integer score;
    private String comment;
    private Rating.RatingType type;
    private ZonedDateTime createdAt;
    private UserResponse rater; // 評分者信息
    private UserResponse ratedUser; // 被評分者信息

    public static RatingResponse fromRating(Rating rating) {
        RatingResponse response = new RatingResponse();
        response.setId(rating.getId());
        response.setOrderId(rating.getOrderId());
        response.setRaterId(rating.getRaterId());
        response.setRatedUserId(rating.getRatedUserId());
        response.setScore(rating.getScore());
        response.setComment(rating.getComment());
        response.setType(rating.getType());
        response.setCreatedAt(rating.getCreatedAt());
        return response;
    }
} 