package com.p2p.dto.webhook;

import lombok.Data;

import java.util.Map;

@Data
public class StripeWebhookEvent {
    private String id;
    private String type;
    private Map<String, Object> data;
    private Long created;
    private boolean livemode;
    private Map<String, Object> request;
    private String pendingWebhooks;
    private String apiVersion;
    private String accountId;

    @Data
    public static class PaymentIntentData {
        private String id;
        private String status;
        private Long amount;
        private String currency;
        private Map<String, String> metadata;
        private String paymentMethod;
        private String clientSecret;
        private String description;
        private boolean canceled;
        private String cancellationReason;
    }

    @Data
    public static class RefundData {
        private String id;
        private String status;
        private Long amount;
        private String currency;
        private String paymentIntent;
        private String reason;
        private Map<String, String> metadata;
    }
} 