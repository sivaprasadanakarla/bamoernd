package com.company.fraud.bamoe.client;

import com.company.fraud.bamoe.domain.FraudDecision;
import com.company.fraud.bamoe.domain.TransactionContext;

public interface BamoeDecisionClient {

    FraudDecision evaluate(TransactionContext context);
}
