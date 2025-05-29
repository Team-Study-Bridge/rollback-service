package org.example.rollbackservice.discount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountRollbackFailureSaver {

    private final DiscountRollbackFailureRepository repository;

    public Mono<Void> save(Long productId, Long purchaseId, String reason) {
        DiscountRollbackFailure failure = DiscountRollbackFailure.builder()
                .productId(productId)
                .purchaseId(purchaseId)
                .reason(reason)
                .createdAt(Instant.now())
                .build();

        return repository.save(failure)
                .doOnSuccess(f -> log.info("✅ 할인 복구 실패 내역 저장 완료 - id={}", f.id()))
                .doOnError(e -> log.error("❌ 할인 복구 실패 내역 저장 중 오류 발생", e))
                .then();
    }
}
