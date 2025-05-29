package org.example.rollbackservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rollbackservice.discount.DiscountRollbackFailureSaver;
import org.example.rollbackservice.event.RollBackRequestMessage;
import org.example.rollbackservice.event.RollBackResponseMessage;
import org.example.rollbackservice.lecture.LectureClient;
import org.example.rollbackservice.portone.PortOneCancelResponse;
import org.example.rollbackservice.portone.PortOneClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RollBackFunctionConfig {

    private final PortOneClient portOneClient;
    private final LectureClient lectureClient;
    private final DiscountRollbackFailureSaver saver;

    @Bean
    public Function<Flux<RollBackRequestMessage>, Flux<RollBackResponseMessage>> rollback() {
        return flux -> flux
                .distinct(RollBackRequestMessage::purchaseId)
                .flatMap(request ->
                        portOneClient.cancelPayment(request.impUid(), request.paidAmount())
                                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                                .flatMap(rsp -> {
                                    PortOneCancelResponse.CancelResponse res = rsp.response();
                                    if (res == null) {
                                        String reason = String.format("포트원에서 보내 줄 응답 없음 (message=%s)", rsp.message());
                                        throw new IllegalStateException(reason);
                                    }
                                    log.info("[롤백 완료] impUid={}, amount={}", res.imp_uid(), res.amount());
                                    return lectureClient.rollbackDiscount(request.productId())
                                            .thenReturn(new RollBackResponseMessage(
                                                    request.purchaseId(), true, "결제 롤백 완료 + 할인 복구 완료"
                                            ))
                                            .onErrorResume(e -> {
                                                // 할인 정보가 없거나, 할인 기간이 지난 경우
                                                if (e instanceof WebClientResponseException.NotFound || e instanceof WebClientResponseException.BadRequest) {
                                                    log.warn("❗ 복구 대상이 아님 (productId={}) → 저장 생략", request.productId());
                                                    return Mono.just(new RollBackResponseMessage(request.purchaseId(), true, "복구 생략: " + e.getMessage()));
                                                }

                                                String reason = "할인 수량 복구 실패: " + e.getMessage();
                                                return saver.save(request.productId(), request.purchaseId(), reason)
                                                        .thenReturn(new RollBackResponseMessage(request.purchaseId(), true, reason));
                                            });
                                })
                                .onErrorResume(e -> {
                                    String fullMessage = extractDeepestMessage(e);
                                    String reason = "[롤백 실패] " + fullMessage;
                                    log.error(reason);
                                    return Mono.just(new RollBackResponseMessage(request.purchaseId(), false, reason));
                                })
                );
    }

    private String extractDeepestMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}
