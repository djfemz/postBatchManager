package com.aspacelife.postbatch.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class WireMockTestConfig {
    @Bean
    @Primary
    public WebClient mockWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:9999")
                .build();
    }
}
