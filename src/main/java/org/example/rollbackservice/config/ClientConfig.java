package org.example.rollbackservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    WebClient webClient(@Value("${webclient.portone-uri}") String portOneUri) {
        return WebClient.builder()
                .baseUrl(portOneUri)
                .build();
    }

    @Bean
    WebClient lectureWebClient(@Value("${webclient.lecture-uri}") String lectureUri) {
        return WebClient.builder()
                .baseUrl(lectureUri)
                .build();
    }
}
