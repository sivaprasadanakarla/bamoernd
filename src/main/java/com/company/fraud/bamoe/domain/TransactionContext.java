package com.company.fraud.bamoe.domain;

import java.math.BigDecimal;

public record TransactionContext(
        String transactionId,
        String customerId,
        BigDecimal amount,
        String currency,
        String channel,
        boolean deviceTrusted,
        double geoVelocityKmPerHour,
        double historicalChargebackRatio,
        int accountAgeDays,
        double ipReputationRisk,
        String merchantCategory
) {
}
