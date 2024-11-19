package com.p2p.util;

import com.p2p.exception.InvalidOperationException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class ValidationUtils {

    public static Mono<Void> validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidOperationException("Price must be greater than 0"));
        }
        return Mono.empty();
    }

    public static Mono<Void> validateDeadline(ZonedDateTime deadline) {
        if (deadline == null || deadline.isBefore(ZonedDateTime.now())) {
            return Mono.error(new InvalidOperationException("Deadline must be in the future"));
        }
        return Mono.empty();
    }

    public static Mono<Void> validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Mono.error(new InvalidOperationException("Quantity must be greater than 0"));
        }
        return Mono.empty();
    }

    public static Mono<Void> validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            return Mono.error(new InvalidOperationException("Rating must be between 1 and 5"));
        }
        return Mono.empty();
    }
} 