package org.example.rollbackservice.discount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rollbackservice.lecture.LectureClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountRollbackRetryScheduler {

    private final DiscountRollbackFailureRepository failureRepository;
    private final LectureClient lectureClient;

    @Scheduled(fixedDelay = 60 * 1000)
    public void retryRollbackFailures() {
        log.info("⏰ 할인권 재고 복구 스케쥴러를 시작합니다.");

        failureRepository.findAll()
                .flatMap(failure ->
                        lectureClient.rollbackDiscount(failure.productId())
                                .doOnSuccess(v -> log.info("다음 ID에 대해서 할인권 재고 복구에 성공하였습니다. productId={}, purchaseId={}",
                                        failure.productId(), failure.purchaseId()))
                                .thenReturn(failure) // rollback 성공한 항목만 다음 단계로
                                .onErrorResume(e -> {
                                    log.warn("다음 ID에 대해서 할인권 재고 복구에 실패하였습니다. productId={}, 이유={}", failure.productId(), e.getMessage());
                                    return Mono.empty(); // 실패한 항목은 제거 생략
                                })
                )
                .flatMap(failure ->
                        failureRepository.deleteById(failure.id())
                                .doOnSuccess(v -> log.info("복구에 성공하여 DB에서 해당 내역을 삭제하였습니다. id={}", failure.id()))
                )
                .subscribe();
    }
}
