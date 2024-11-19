package com.p2p.exception;

public class InvalidOperationException extends BusinessException {
    
    public InvalidOperationException(String message) {
        super("INVALID_OPERATION", message);
    }

    public InvalidOperationException(String operation, String reason) {
        super("INVALID_OPERATION", String.format("Cannot perform %s: %s", operation, reason));
    }
} 