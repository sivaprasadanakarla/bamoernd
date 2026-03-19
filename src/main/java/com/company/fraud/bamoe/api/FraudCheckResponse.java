package com.company.fraud.bamoe.api;

import com.company.fraud.bamoe.domain.DecisionAction;
import java.util.List;

public record FraudCheckResponse(
        String transactionId,
        int riskScore,
        DecisionAction action,
        List<String> reasons,
        String decisionSource,
        String recommendedNextStep
) {
}
