package com.p2p.exception;

public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super("RESOURCE_NOT_FOUND", String.format("%s with id %d not found", resource, id));
    }
} 