package com.company.fraud.bamoe.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FraudCheckRequest(
        @NotBlank String transactionId,
        @NotBlank String customerId,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String channel,
        boolean deviceTrusted,
        @Min(0) @Max(1500) int geoVelocityKmPerHour,
        @DecimalMin("0.0") double historicalChargebackRatio,
        @Min(0) int accountAgeDays,
        @DecimalMin("0.0") @Max(1) double ipReputationRisk,
        @NotBlank String merchantCategory
) {
}
