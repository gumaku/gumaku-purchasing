package com.p2p.exception;

import com.p2p.constant.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ErrorResponse> handleUnauthorized(UnauthorizedException ex, ServerWebExchange exchange) {
        log.error("Unauthorized error: {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ErrorResponse> handleAccessDenied(AccessDeniedException ex, ServerWebExchange exchange) {
        log.error("Access denied error: {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(
            HttpStatus.FORBIDDEN.value(),
            "访问被拒绝",
            exchange.getRequest().getPath().value()
        ));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationErrors(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Mono.just(ResponseEntity.badRequest().body(errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, ServerWebExchange exchange) {
        log.error("Invalid argument error: {}", ex.getMessage());
        return Mono.just(ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        ));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGeneral(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error occurred", ex);
        return Mono.just(ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "服务器内部错误，请稍后重试",
            exchange.getRequest().getPath().value()
        ).withTrace(ex.getMessage()));
    }
} 