package com.company.fraud.bamoe.client;

import com.company.fraud.bamoe.domain.DecisionAction;
import com.company.fraud.bamoe.domain.FraudDecision;
import com.company.fraud.bamoe.domain.TransactionContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockBamoeDecisionClient implements BamoeDecisionClient {

    private static final Logger log = LoggerFactory.getLogger(MockBamoeDecisionClient.class);

    @Override
    public FraudDecision evaluate(TransactionContext context) {
        log.debug("[bamoe-mock] Evaluating transactionId={} with mock rules", context.transactionId());

        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (context.amount().compareTo(new BigDecimal("5000")) > 0) {
            score += 35;
            reasons.add("High-value transaction");
        }

        if (!context.deviceTrusted()) {
            score += 20;
            reasons.add("Untrusted device");
        }

        if (context.geoVelocityKmPerHour() > 700) {
            score += 25;
            reasons.add("Impossible travel pattern");
        }

        if (context.historicalChargebackRatio() > 0.12) {
            score += 20;
            reasons.add("Elevated historical chargeback ratio");
        }

        if (context.accountAgeDays() < 14) {
            score += 15;
            reasons.add("Newly created account");
        }

        if (context.ipReputationRisk() > 0.7) {
            score += 30;
            reasons.add("High-risk IP reputation");
        }

        score = Math.min(score, 100);

        DecisionAction action;
        if (score >= 75) {
            action = DecisionAction.BLOCK;
        } else if (score >= 45) {
            action = DecisionAction.REVIEW;
        } else {
            action = DecisionAction.APPROVE;
        }

        if (reasons.isEmpty()) {
            reasons.add("No major fraud signals triggered");
        }

        log.debug("[bamoe-mock] Evaluation complete transactionId={} action={} riskScore={} reasonsCount={}",
                context.transactionId(), action, score, reasons.size());
        return new FraudDecision(score, action, reasons, "BAMOE_MOCK");
    }
}
