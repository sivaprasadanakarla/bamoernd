package com.company.fraud.bamoe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FraudPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudPocApplication.class, args);
    }
}
