package org.example.rollbackservice.discount;

import org.example.rollbackservice.lecture.LectureClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscountRollbackRetrySchedulerTest {

    private DiscountRollbackFailureRepository repository;
    private LectureClient lectureClient;
    private DiscountRollbackRetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        repository = mock(DiscountRollbackFailureRepository.class);
        lectureClient = mock(LectureClient.class);
        scheduler = new DiscountRollbackRetryScheduler(repository, lectureClient);
    }

    @Test
    @DisplayName("복구 재시도가 성공하면 로그가 삭제된다")
    void retrySuccessDeletesLog() {
        DiscountRollbackFailure failure = DiscountRollbackFailure.builder()
                .id(1L)
                .productId(101L)
                .purchaseId(202L)
                .reason("에러")
                .build();

        when(repository.findAll()).thenReturn(Flux.just(failure));
        when(lectureClient.rollbackDiscount(101L)).thenReturn(Mono.empty());
        when(repository.deleteById(1L)).thenReturn(Mono.empty());

        scheduler.retryRollbackFailures();

        verify(lectureClient, timeout(500).times(1)).rollbackDiscount(101L);
        verify(repository, timeout(500).times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("복구 재시도가 실패해도 다음 항목으로 넘어간다")
    void retryFailureContinues() {
        DiscountRollbackFailure failure = DiscountRollbackFailure.builder()
                .id(2L)
                .productId(102L)
                .purchaseId(203L)
                .reason("재시도 실패용")
                .build();

        when(repository.findAll()).thenReturn(Flux.just(failure));
        when(lectureClient.rollbackDiscount(102L)).thenReturn(Mono.error(new RuntimeException("강제 에러")));

        scheduler.retryRollbackFailures();

        // ✅ sleep으로 비동기 로직 대기
        try {
            Thread.sleep(300); // ms 단위. 필요시 500~1000으로 늘려도 됨
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 테스트 종료 시 안전 처리
        }

        verify(lectureClient, times(1)).rollbackDiscount(102L);
        verify(repository, never()).deleteById(any(Long.class));
    }
}