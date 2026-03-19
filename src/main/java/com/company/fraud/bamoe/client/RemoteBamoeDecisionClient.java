package com.company.fraud.bamoe.client;

import com.company.fraud.bamoe.domain.DecisionAction;
import com.company.fraud.bamoe.domain.FraudDecision;
import com.company.fraud.bamoe.domain.TransactionContext;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

public class RemoteBamoeDecisionClient implements BamoeDecisionClient {

    private static final Logger log = LoggerFactory.getLogger(RemoteBamoeDecisionClient.class);

    private final RestClient restClient;
    private final String endpointPath;
    private final String apiKey;

    public RemoteBamoeDecisionClient(RestClient restClient, String endpointPath, String apiKey) {
        this.restClient = restClient;
        this.endpointPath = endpointPath;
        this.apiKey = apiKey;
    }

    @Override
    public FraudDecision evaluate(TransactionContext context) {
        log.debug("[bamoe-remote] Calling remote BAMOE endpoint transactionId={} endpointPath={}",
            context.transactionId(), endpointPath);

        BamoeDecisionResponse response = restClient.post()
                .uri(endpointPath)
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    if (StringUtils.hasText(apiKey)) {
                        headers.setBearerAuth(apiKey);
                    }
                })
                .body(new BamoeDecisionRequest(context))
                .retrieve()
                .body(BamoeDecisionResponse.class);

        if (response == null) {
            throw new IllegalStateException("No response returned by BAMOE decision service");
        }

        log.debug("[bamoe-remote] Remote response received transactionId={} action={} riskScore={} reasonsCount={}",
            context.transactionId(), response.action(), response.riskScore(),
            response.reasons() == null ? 0 : response.reasons().size());
        return new FraudDecision(
                response.riskScore(),
                DecisionAction.valueOf(response.action()),
                response.reasons() == null ? List.of("No reasons returned") : response.reasons(),
                "BAMOE_REMOTE"
        );
    }

    private record BamoeDecisionRequest(TransactionContext context) {
    }

    private record BamoeDecisionResponse(int riskScore, String action, List<String> reasons) {
    }
}
