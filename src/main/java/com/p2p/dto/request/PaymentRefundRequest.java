package com.p2p.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class PaymentRefundRequest {
    @NotNull(message = "Payment ID is required")
    private Long paymentId;
    
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
} 