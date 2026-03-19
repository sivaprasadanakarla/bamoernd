package com.company.fraud.bamoe.api;

import com.company.fraud.bamoe.service.FraudScreeningService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fraud")
public class FraudScreeningController {

    private static final Logger log = LoggerFactory.getLogger(FraudScreeningController.class);

    private final FraudScreeningService fraudScreeningService;

    public FraudScreeningController(FraudScreeningService fraudScreeningService) {
        this.fraudScreeningService = fraudScreeningService;
    }

    @PostMapping("/screen")
    public ResponseEntity<FraudCheckResponse> screen(@Valid @RequestBody FraudCheckRequest request) {
        log.debug("[controller] Received fraud screening request transactionId={} customerId={} amount={} channel={}",
            request.transactionId(), request.customerId(), request.amount(), request.channel());

        FraudCheckResponse response = fraudScreeningService.evaluate(request);

        log.debug("[controller] Sending fraud screening response transactionId={} action={} riskScore={} source={}",
            response.transactionId(), response.action(), response.riskScore(), response.decisionSource());
        return ResponseEntity.ok(response);
    }
}
