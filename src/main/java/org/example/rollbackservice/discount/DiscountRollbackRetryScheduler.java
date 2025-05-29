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
//r
    private final DiscountRollbackFailureRepository failureRepository;
    private final LectureClient lectureClient;

    @Scheduled(fixedDelay = 60 * 1000)
    public void retryRollbackFailures() {
        log.info("⏰ [할인 복구 재시도] 시작");

        failureRepository.findAll()
                .flatMap(failure ->
                        lectureClient.rollbackDiscount(failure.productId())
                                .doOnSuccess(v -> log.info("✅ [복구 재시도 성공] productId={}, purchaseId={}", failure.productId(), failure.purchaseId()))
                                .thenReturn(failure) // rollback 성공한 항목만 다음 단계로
                                .onErrorResume(e -> {
                                    log.warn("❌ [복구 재시도 실패] productId={}, 이유={}", failure.productId(), e.getMessage());
                                    return Mono.empty(); // 실패한 항목은 제거 생략
                                })
                )
                .flatMap(failure ->
                        failureRepository.deleteById(failure.id())
                                .doOnSuccess(v -> log.info("🗑️ [실패 로그 삭제 완료] id={}", failure.id()))
                )
                .subscribe();
    }
}
