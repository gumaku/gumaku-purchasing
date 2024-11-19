package com.p2p.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentStatisticsResponse {
    private BigDecimal totalAmount;
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private BigDecimal averageTransactionAmount;
    private Map<String, BigDecimal> amountByStatus;
    private Map<String, Long> transactionsByMonth;
    private Map<String, BigDecimal> revenueByMonth;
} 