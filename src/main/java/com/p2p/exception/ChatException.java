package com.p2p.exception;

public class ChatException extends BusinessException {
    
    public ChatException(String message) {
        super("CHAT_ERROR", message);
    }

    public static class ChatRoomNotFoundException extends ChatException {
        public ChatRoomNotFoundException(Long roomId) {
            super(String.format("Chat room with id %d not found", roomId));
        }
    }

    public static class UnauthorizedChatAccessException extends ChatException {
        public UnauthorizedChatAccessException(Long userId, Long roomId) {
            super(String.format("User %d is not authorized to access chat room %d", userId, roomId));
        }
    }

    public static class MessageDeletionException extends ChatException {
        public MessageDeletionException(String message) {
            super(message);
        }
    }
} 