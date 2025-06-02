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
                .doOnSuccess(f -> log.info("할인권 재고 복구에 대해 실패한 내역을 저장하였습니다. id={}", f.id()))
                .doOnError(e -> log.error("할인권 재고 복구에 대한 실패 내역을 저장하는 중에 오류가 발생하였습니다.", e))
                .then();
    }
}
