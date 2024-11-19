package com.p2p.constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int code;
    private String message;
    private long timestamp;
    private String path;
    private String trace;

    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ErrorResponse(int code, String message, String path) {
        this(code, message);
        this.path = path;
    }

    public static ErrorResponse of(int code, String message) {
        return new ErrorResponse(code, message);
    }

    public static ErrorResponse of(int code, String message, String path) {
        return new ErrorResponse(code, message, path);
    }

    public ErrorResponse withTrace(String trace) {
        this.trace = trace;
        return this;
    }
} 