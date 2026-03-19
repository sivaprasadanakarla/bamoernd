package com.company.fraud.bamoe.service;

import com.company.fraud.bamoe.api.FraudCheckRequest;
import com.company.fraud.bamoe.api.FraudCheckResponse;
import com.company.fraud.bamoe.client.BamoeDecisionClient;
import com.company.fraud.bamoe.domain.DecisionAction;
import com.company.fraud.bamoe.domain.FraudDecision;
import com.company.fraud.bamoe.domain.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FraudScreeningService {

    private static final Logger log = LoggerFactory.getLogger(FraudScreeningService.class);

    private final BamoeDecisionClient bamoeDecisionClient;

    public FraudScreeningService(BamoeDecisionClient bamoeDecisionClient) {
        this.bamoeDecisionClient = bamoeDecisionClient;
    }

    public FraudCheckResponse evaluate(FraudCheckRequest request) {
        log.debug("[service] Building transaction context transactionId={}", request.transactionId());

        TransactionContext context = new TransactionContext(
                request.transactionId(),
                request.customerId(),
                request.amount(),
                request.currency(),
                request.channel(),
                request.deviceTrusted(),
                request.geoVelocityKmPerHour(),
                request.historicalChargebackRatio(),
                request.accountAgeDays(),
                request.ipReputationRisk(),
                request.merchantCategory()
        );

            log.debug("[service] Calling BAMOE decision client transactionId={} modeContextReady=true", request.transactionId());
        FraudDecision decision = bamoeDecisionClient.evaluate(context);
            log.debug("[service] Decision received transactionId={} action={} riskScore={} reasonsCount={} source={}",
                request.transactionId(), decision.action(), decision.riskScore(), decision.reasons().size(), decision.decisionSource());

            FraudCheckResponse response = new FraudCheckResponse(
                request.transactionId(),
                decision.riskScore(),
                decision.action(),
                decision.reasons(),
                decision.decisionSource(),
                nextStep(decision.action())
        );

            log.debug("[service] Response assembled transactionId={} nextStep={}", request.transactionId(), response.recommendedNextStep());
            return response;
    }

    private String nextStep(DecisionAction action) {
        return switch (action) {
            case APPROVE -> "Auto-approve transaction";
            case REVIEW -> "Route to fraud analyst queue";
            case BLOCK -> "Block transaction and open incident";
        };
    }
}
