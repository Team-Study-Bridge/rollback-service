package org.example.rollbackservice.lecture;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LectureClient {

    private final WebClient lectureWebClient;

    public Mono<Void> rollbackDiscount(Long productId) {
        return lectureWebClient.post()
                .uri("/lecture-discounts/{id}/rollback", productId)
                .retrieve()
                .onStatus(status -> status.isError(), res -> Mono.error(new RuntimeException("할인 복구 실패")))
                .toBodilessEntity()
                .then();
    }
}
