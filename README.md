# IBM BAMOE 9.x Fraud PoC (Spring Boot)

This PoC demonstrates how an internal fraud application can use IBM BAMOE 9.x as a centralized decisioning layer.

## What this PoC shows

- A Spring Boot fraud-screening API for transaction checks.
- A BAMOE integration point via `BamoeDecisionClient`.
- `mock` mode for local demos without BAMOE runtime.
- `local-dmn` mode to execute DMN using embedded KIE/BAMOE runtime jars.
- `remote` mode to call a deployed BAMOE decision service endpoint.
- A starter DMN artifact at `src/main/resources/bamoe/fraud-decision.dmn`.

## Architecture

1. `POST /api/fraud/screen` receives transaction signals.
2. `FraudScreeningService` maps request to `TransactionContext`.
3. `BamoeDecisionClient` evaluates decision:
  - `MockBamoeDecisionClient` for local PoC.
  - `EmbeddedDmnBamoeDecisionClient` for local DMN execution with KIE runtime.
  - `RemoteBamoeDecisionClient` for BAMOE-hosted decision service.
4. API returns risk score, action, reasons, and next step.

## Run locally

### Prerequisites

- Java 17+
- Maven 3.9+

### Start

```bash
mvn spring-boot:run
```

## Demo request

```bash
curl -X POST http://localhost:8080/api/fraud/screen \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId":"tx-9001",
    "customerId":"cust-77",
    "amount":9500,
    "currency":"USD",
    "channel":"MOBILE",
    "deviceTrusted":false,
    "geoVelocityKmPerHour":900,
    "historicalChargebackRatio":0.18,
    "accountAgeDays":3,
    "ipReputationRisk":0.93,
    "merchantCategory":"ELECTRONICS"
  }'
```

Expected outcome in local-dmn/mock mode: high risk score with `BLOCK` action.

## Use embedded BAMOE/KIE jars (local DMN)

This PoC now includes KIE DMN runtime dependencies and executes `src/main/resources/bamoe/fraud-decision.dmn` locally.

Default mode is already set to:

```yaml
bamoe:
  mode: local-dmn
```

So starting the app runs DMN decisioning locally without a separate BAMOE server.

## Switch to BAMOE remote mode

Update `src/main/resources/application.yml`:

```yaml
bamoe:
  mode: remote
  base-url: http://<bamoe-host>:<port>
  endpoint-path: /fraud-decision
  api-key: "<token-if-required>"
```

In remote mode, the PoC calls BAMOE decision service over REST. You can deploy your DMN/rule assets in BAMOE and point this app to that service.

## Suggested company demo storyline

1. Show current fraud pain points (inconsistent analyst decisions, false positives).
2. Run PoC in `mock` mode to show API and end-to-end fraud actioning.
3. Switch to `remote` mode and call BAMOE-hosted decision service.
4. Explain business-owned rule updates in BAMOE without redeploying app code.
5. Highlight auditability: risk score + reason list from a governed decision model.

## Next evolution path

- Replace mock scoring with production DMN decision table in BAMOE.
- Add case management/BPM flow for `REVIEW` outcomes.
- Persist decision trail for compliance and model monitoring.
- Add A/B model versions for fraud strategy experiments.
