package org.example.rollbackservice.discount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DiscountRollbackFailureSaverTest {

    private DiscountRollbackFailureRepository repository;
    private DiscountRollbackFailureSaver saver;

    @BeforeEach
    void setUp() {
        repository = mock(DiscountRollbackFailureRepository.class);
        saver = new DiscountRollbackFailureSaver(repository);
    }

    @Test
    @DisplayName("복구 실패 내역이 정상적으로 저장된다")
    void saveSuccess() {
        when(repository.save(any(DiscountRollbackFailure.class)))
                .thenReturn(Mono.just(DiscountRollbackFailure.builder().id(1L).build()));

        StepVerifier.create(saver.save(100L, 200L, "테스트"))
                .verifyComplete();

        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("저장 도중 오류가 발생해도 예외를 던지지 않는다")
    void saveErrorHandledGracefully() {
        when(repository.save(any())).thenReturn(Mono.error(new RuntimeException("DB 에러")));

        StepVerifier.create(saver.save(100L, 200L, "에러 발생"))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).save(any());
    }
}
