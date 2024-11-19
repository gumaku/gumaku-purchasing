package com.p2p.service.impl;

import com.p2p.domain.Rating;
import com.p2p.repository.RatingRepository;
import com.p2p.service.NotificationService;
import com.p2p.service.RatingService;
import com.p2p.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Mono<Rating> createRating(Rating rating) {
        rating.preSave();
        return ratingRepository.save(rating)
            .flatMap(savedRating -> 
                updateUserCreditScore(savedRating.getRatedUserId())
                    .thenReturn(savedRating)
            )
            .doOnSuccess(savedRating -> {
                notificationService.createRatingNotification(
                    savedRating.getRatedUserId(),
                    String.format("您收到了一個新的%d星評價", savedRating.getScore()),
                    savedRating.getId()
                ).subscribe();
            });
    }

    @Override
    public Flux<Rating> getUserRatings(Long userId, int page, int size) {
        return ratingRepository.findByRatedUserIdOrderByCreatedAtDesc(userId)
            .skip(page * size)
            .take(size);
    }

    @Override
    public Mono<Double> getUserAverageRating(Long userId) {
        return ratingRepository.calculateAverageRating(userId);
    }

    @Override
    public Mono<Rating> getOrderRating(Long orderId, Long raterId) {
        return ratingRepository.findByOrderIdAndRaterId(orderId, raterId);
    }

    @Override
    public Flux<Rating> getLatestRatings(Long userId, int limit) {
        return ratingRepository.findCompletedOrderRatings(userId, limit);
    }

    @Override
    public Mono<Map<Integer, Long>> getRatingDistribution(Long userId) {
        return ratingRepository.getRatingDistribution(userId);
    }

    @Override
    public Mono<Boolean> canRateOrder(Long orderId, Long raterId) {
        return ratingRepository.existsByOrderIdAndRaterId(orderId, raterId)
            .map(exists -> !exists);
    }

    @Override
    @Transactional
    public Mono<Rating> updateRating(Long ratingId, Rating rating) {
        return ratingRepository.findById(ratingId)
            .filter(existingRating -> existingRating.canBeModified(rating.getRaterId()))
            .flatMap(existingRating -> {
                existingRating.setScore(rating.getScore());
                existingRating.setComment(rating.getComment());
                return ratingRepository.save(existingRating)
                    .flatMap(savedRating -> 
                        updateUserCreditScore(savedRating.getRatedUserId())
                            .thenReturn(savedRating)
                    );
            });
    }

    @Override
    @Transactional
    public Mono<Void> deleteRating(Long ratingId, Long userId) {
        return ratingRepository.findById(ratingId)
            .filter(rating -> rating.canBeModified(userId))
            .flatMap(rating -> ratingRepository.delete(rating));
    }

    @Override
    public Mono<Long> countUserRatings(Long userId) {
        return ratingRepository.countByRatedUserId(userId);
    }

    @Override
    public Mono<Map<String, Double>> getRatingAveragesByType(Long userId) {
        return ratingRepository.getRatingAveragesByType(userId)
            .collectMap(
                RatingRepository.RatingTypeAverage::getType,
                RatingRepository.RatingTypeAverage::getAverageScore
            );
    }

    @Override
    public Mono<Long> countHighRatings(Long userId, int minScore) {
        return ratingRepository.countHighRatings(userId, minScore);
    }

    private Mono<Void> updateUserCreditScore(Long userId) {
        return getUserAverageRating(userId)
            .flatMap(averageRating -> 
                userService.updateCreditScore(userId, averageRating)
            )
            .then();
    }
} 