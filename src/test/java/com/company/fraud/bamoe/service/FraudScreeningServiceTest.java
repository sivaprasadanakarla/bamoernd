package com.company.fraud.bamoe.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.fraud.bamoe.api.FraudCheckRequest;
import com.company.fraud.bamoe.client.MockBamoeDecisionClient;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FraudScreeningServiceTest {

    private final FraudScreeningService service = new FraudScreeningService(new MockBamoeDecisionClient());

    @Test
    void shouldBlockClearlyRiskyTransaction() {
        FraudCheckRequest request = new FraudCheckRequest(
                "tx-9001",
                "cust-77",
                new BigDecimal("9500"),
                "USD",
                "MOBILE",
                false,
                900,
                0.18,
                3,
                0.93,
                "ELECTRONICS"
        );

        var response = service.evaluate(request);

        assertThat(response.action().name()).isEqualTo("BLOCK");
        assertThat(response.riskScore()).isGreaterThanOrEqualTo(75);
    }

    @Test
    void shouldApproveLowRiskTransaction() {
        FraudCheckRequest request = new FraudCheckRequest(
                "tx-1100",
                "cust-88",
                new BigDecimal("25"),
                "USD",
                "WEB",
                true,
                10,
                0.01,
                500,
                0.05,
                "GROCERY"
        );

        var response = service.evaluate(request);

        assertThat(response.action().name()).isEqualTo("APPROVE");
        assertThat(response.riskScore()).isLessThan(45);
    }
}
