package com.company.fraud.bamoe.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.fraud.bamoe.domain.TransactionContext;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EmbeddedDmnBamoeDecisionClientTest {

    private final EmbeddedDmnBamoeDecisionClient client = new EmbeddedDmnBamoeDecisionClient();

    @Test
    void shouldEvaluateHighRiskTransactionWithDmn() {
        TransactionContext context = new TransactionContext(
                "tx-2001",
                "cust-11",
                new BigDecimal("9100"),
                "USD",
                "MOBILE",
                false,
                950,
                0.2,
                2,
                0.95,
                "ELECTRONICS"
        );

        var decision = client.evaluate(context);

        assertThat(decision.action().name()).isEqualTo("BLOCK");
        assertThat(decision.riskScore()).isGreaterThanOrEqualTo(75);
        assertThat(decision.decisionSource()).isEqualTo("BAMOE_EMBEDDED_DMN");
    }
}
