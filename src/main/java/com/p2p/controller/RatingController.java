package com.p2p.controller;

import com.p2p.domain.Rating;
import com.p2p.dto.request.RatingCreateRequest;
import com.p2p.dto.response.RatingResponse;
import com.p2p.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public Mono<ResponseEntity<RatingResponse>> createRating(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RatingCreateRequest request) {
        Rating rating = new Rating();
        rating.setOrderId(request.getOrderId());
        rating.setRaterId(userId);
        rating.setRatedUserId(request.getRatedUserId());
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());

        return ratingService.createRating(rating)
            .map(RatingResponse::fromRating)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @GetMapping("/user/{userId}")
    public Flux<RatingResponse> getUserRatings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ratingService.getUserRatings(userId, page, size)
            .map(RatingResponse::fromRating);
    }

    @GetMapping("/user/{userId}/average")
    public Mono<ResponseEntity<Double>> getUserAverageRating(@PathVariable Long userId) {
        return ratingService.getUserAverageRating(userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}/rater/{raterId}")
    public Mono<ResponseEntity<RatingResponse>> getOrderRating(
            @PathVariable Long orderId,
            @PathVariable Long raterId) {
        return ratingService.getOrderRating(orderId, raterId)
            .map(RatingResponse::fromRating)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/latest")
    public Flux<RatingResponse> getLatestRatings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        return ratingService.getLatestRatings(userId, limit)
            .map(RatingResponse::fromRating);
    }

    @DeleteMapping("/{ratingId}")
    public Mono<ResponseEntity<Void>> deleteRating(
            @PathVariable Long ratingId,
            @AuthenticationPrincipal Long userId) {
        return ratingService.deleteRating(ratingId, userId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/statistics/distribution")
    public Mono<ResponseEntity<Map<Integer, Long>>> getRatingDistribution(
            @RequestParam Long userId) {
        return ratingService.getRatingDistribution(userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}/can-rate")
    public Mono<ResponseEntity<Boolean>> canRateOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId) {
        return ratingService.canRateOrder(orderId, userId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
} 