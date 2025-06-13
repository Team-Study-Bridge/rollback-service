# ♻️ rollback-service

Spring WebFlux 기반의 **결제 보상 및 할인 복구 처리 서비스**입니다.  
결제 시스템에서 실패하거나 검증이 불일치한 결제에 대해 **결제 취소 + 할인권 재고 복구**를 비동기적으로 처리합니다.  
또한 복구 실패에 대한 내역을 저장하고, 재시도하는 기능을 제공합니다.

---

## 🚀 주요 기술 스택
- **Spring Boot 3.4**
- **WebFlux + R2DBC** – 논블로킹 비동기 처리
- **RabbitMQ (Spring Cloud Stream)** – 결제 보상 요청 수신
- **PortOne 결제 취소 API 연동**

---

## 🧩 주요 기능

- `payment-service`에서 전송한 **ROLLBACK 요청 메시지 소비**
- PortOne API를 이용한 결제 취소 수행
- `lecture-service`의 할인권 재고 복구 API 호출
- 복구 실패 시 실패 내역(`discount_rollback_failure`) 저장
- `@Scheduled` 기반 자동 재시도 로직 실행
- 복구 제외 대상(기간 종료, 대상 없음 등)은 저장하지 않음

---

## 🧱 내부 구성

| 구성 요소 | 설명 |
|-----------|------|
| `RollBackFunctionConfig` | Function<Flux> 기반 RabbitMQ 메시지 처리 로직 |
| `DiscountRollbackFailureSaver` | 복구 실패 내역을 DB에 저장 |
| `DiscountRollbackRetryScheduler` | 복구 실패 내역을 일정 주기마다 재시도 |
| `LectureClient` | 강의 서비스 WebClient 호출 |
| `PortOneClient` | 결제 취소 요청 WebClient 호출 |

---

## ⚠ 예외 처리 및 관리 기능

- 포트원 응답이 `null`이거나 오류 발생 시 `결제 취소 실패`로 간주하여 응답 생성
- 할인 복구 대상이 없는 경우(`404`, `400`)는 **정상 종료 처리**
- 기타 예외 발생 시 복구 실패 내역 저장 후 `재시도 스케줄러`에 의해 반복 시도
- 동일한 `purchaseId`에 대해 중복 처리를 방지하기 위한 `distinct` 처리

---

## 📡 의존 서비스

| 서비스명         | 역할                            |
|------------------|---------------------------------|
| `payment-service` | 결제 상태 검증 및 ROLLBACK 이벤트 발행 |
| `lecture-service` | 할인권 재고 복구 API 제공         |
| `PortOne`         | 결제 취소 수행 (PG사 연동)       |