package com.company.fraud.bamoe.domain;

import java.util.List;

public record FraudDecision(
        int riskScore,
        DecisionAction action,
        List<String> reasons,
        String decisionSource
) {
}
