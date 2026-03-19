package com.company.fraud.bamoe.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bamoe")
public record BamoeProperties(
        Mode mode,
        @NotBlank String baseUrl,
        @NotBlank String endpointPath,
        @Min(100) int timeoutMs,
        String apiKey
) {
    public enum Mode {
        MOCK,
        LOCAL_DMN,
        REMOTE
    }
}
