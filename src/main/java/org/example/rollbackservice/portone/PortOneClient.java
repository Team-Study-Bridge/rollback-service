package org.example.rollbackservice.portone;

import lombok.RequiredArgsConstructor;
import org.example.rollbackservice.config.PortOneProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final WebClient webClient;
    private final PortOneProperties portOneProperties;

    public Mono<PortOneCancelResponse> cancelPayment(String impUid, int amount) {
        return getAccessToken()
                .flatMap(token ->
                        webClient.post()
                                .uri("/payments/cancel")
                                .header("Authorization", "Bearer " + token)
                                .bodyValue(Map.of(
                                        "imp_uid", impUid,
                                        "amount", amount
                                ))
                                .retrieve()
                                .bodyToMono(PortOneCancelResponse.class)
                );
    }

    private Mono<String> getAccessToken() {
        return webClient.post()
                .uri("/users/getToken")
                .bodyValue(Map.of(
                        "imp_key", portOneProperties.impKey(),
                        "imp_secret", portOneProperties.impSecret()
                ))
                .retrieve()
                .bodyToMono(PortOneTokenResponse.class)
                .map(token -> token.response().access_token());
    }

}
