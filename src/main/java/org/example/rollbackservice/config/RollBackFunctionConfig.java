package org.example.rollbackservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rollbackservice.event.RollBackRequestMessage;
import org.example.rollbackservice.event.RollBackResponseMessage;
import org.example.rollbackservice.portone.PortOneCancelResponse;
import org.example.rollbackservice.portone.PortOneClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public Function<Flux<RollBackRequestMessage>, Flux<RollBackResponseMessage>> rollback() {
        return flux -> flux
                .distinct(RollBackRequestMessage::purchaseId)
                .flatMap(request ->
                portOneClient.cancelPayment(request.impUid(), request.amount())
                        .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                        .map(rsp -> {
                            PortOneCancelResponse.CancelResponse res = rsp.response();
                            if (res == null) {
                                String reason = String.format("포트원에서 보내 줄 응답 없음 (message=%s)", rsp.message());
                                throw new IllegalStateException(reason);
                            }
                            log.info("[롤백 완료] impUid={}, amount={}", res.imp_uid(), res.amount());
                            return new RollBackResponseMessage(request.purchaseId(), true, "롤백 완료 : 결제 취소 처리");
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
