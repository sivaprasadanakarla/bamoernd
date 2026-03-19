package com.company.fraud.bamoe.config;

import com.company.fraud.bamoe.client.BamoeDecisionClient;
import com.company.fraud.bamoe.client.EmbeddedDmnBamoeDecisionClient;
import com.company.fraud.bamoe.client.MockBamoeDecisionClient;
import com.company.fraud.bamoe.client.RemoteBamoeDecisionClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BamoeClientConfig {

    @Bean
    BamoeDecisionClient bamoeDecisionClient(BamoeProperties properties) {
        if (properties.mode() == null || properties.mode() == BamoeProperties.Mode.MOCK) {
            return new MockBamoeDecisionClient();
        }

        if (properties.mode() == BamoeProperties.Mode.LOCAL_DMN) {
            return new EmbeddedDmnBamoeDecisionClient();
        }

        if (properties.mode() == BamoeProperties.Mode.REMOTE) {
            RestClient restClient = RestClient.builder()
                    .baseUrl(properties.baseUrl())
                    .build();
            return new RemoteBamoeDecisionClient(restClient, properties.endpointPath(), properties.apiKey());
        }

        throw new IllegalStateException("Unsupported BAMOE mode: " + properties.mode());
    }
}
