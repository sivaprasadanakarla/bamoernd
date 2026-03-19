package com.company.fraud.bamoe.client;

import com.company.fraud.bamoe.domain.DecisionAction;
import com.company.fraud.bamoe.domain.FraudDecision;
import com.company.fraud.bamoe.domain.TransactionContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.internal.io.ResourceFactory;

public class EmbeddedDmnBamoeDecisionClient implements BamoeDecisionClient {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedDmnBamoeDecisionClient.class);

    private static final String DMN_CLASSPATH = "bamoe/fraud-decision.dmn";
    private static final String MODEL_NAMESPACE = "https://company.internal/fraud/bamoe";
    private static final String MODEL_NAME = "FraudDecisionModel";

    private final DMNRuntime dmnRuntime;
    private final DMNModel dmnModel;

    public EmbeddedDmnBamoeDecisionClient() {
        log.debug("[bamoe-embedded] Initializing embedded DMN runtime dmnPath={}", DMN_CLASSPATH);
        this.dmnRuntime = buildRuntime();
        this.dmnModel = dmnRuntime.getModel(MODEL_NAMESPACE, MODEL_NAME);
        if (this.dmnModel == null) {
            throw new IllegalStateException("Unable to find DMN model " + MODEL_NAMESPACE + " / " + MODEL_NAME);
        }
        log.debug("[bamoe-embedded] Loaded DMN model namespace={} model={}", MODEL_NAMESPACE, MODEL_NAME);
    }

    @Override
    public FraudDecision evaluate(TransactionContext context) {
        log.debug("[bamoe-embedded] Evaluating transactionId={} with embedded DMN", context.transactionId());

        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("transaction", toTransactionMap(context));

        DMNResult result = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        if (result.hasErrors()) {
            throw new IllegalStateException("DMN evaluation failed: " + result.getMessages());
        }

        int riskScore = asInt(result.getContext().get("riskScore"));
        DecisionAction action = DecisionAction.valueOf(String.valueOf(result.getContext().get("decisionAction")));

        FraudDecision decision = new FraudDecision(
                Math.min(Math.max(riskScore, 0), 100),
                action,
                buildReasons(context),
                "BAMOE_EMBEDDED_DMN"
        );

        log.debug("[bamoe-embedded] DMN evaluation complete transactionId={} action={} riskScore={} reasonsCount={}",
            context.transactionId(), decision.action(), decision.riskScore(), decision.reasons().size());
        return decision;
    }

    private DMNRuntime buildRuntime() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write("src/main/resources/" + DMN_CLASSPATH, ResourceFactory.newClassPathResource(DMN_CLASSPATH));

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("DMN build failed: " + results.getMessages(Message.Level.ERROR));
        }

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        KieBase kieBase = kieContainer.getKieBase();
        return KieRuntimeFactory.of(kieBase).get(DMNRuntime.class);
    }

    private Map<String, Object> toTransactionMap(TransactionContext context) {
        Map<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("transactionId", context.transactionId());
        transaction.put("customerId", context.customerId());
        transaction.put("amount", context.amount());
        transaction.put("currency", context.currency());
        transaction.put("channel", context.channel());
        transaction.put("deviceTrusted", context.deviceTrusted());
        transaction.put("geoVelocityKmPerHour", context.geoVelocityKmPerHour());
        transaction.put("historicalChargebackRatio", context.historicalChargebackRatio());
        transaction.put("accountAgeDays", context.accountAgeDays());
        transaction.put("ipReputationRisk", context.ipReputationRisk());
        transaction.put("merchantCategory", context.merchantCategory());
        return transaction;
    }

    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private List<String> buildReasons(TransactionContext context) {
        List<String> reasons = new ArrayList<>();

        if (context.amount().doubleValue() > 5000) {
            reasons.add("High-value transaction");
        }
        if (!context.deviceTrusted()) {
            reasons.add("Untrusted device");
        }
        if (context.geoVelocityKmPerHour() > 700) {
            reasons.add("Impossible travel pattern");
        }
        if (context.historicalChargebackRatio() > 0.12) {
            reasons.add("Elevated historical chargeback ratio");
        }
        if (context.accountAgeDays() < 14) {
            reasons.add("Newly created account");
        }
        if (context.ipReputationRisk() > 0.7) {
            reasons.add("High-risk IP reputation");
        }

        if (reasons.isEmpty()) {
            reasons.add("No major fraud signals triggered");
        }
        return reasons;
    }
}
