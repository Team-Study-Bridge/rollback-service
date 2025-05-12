package org.example.rollbackservice.discount;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DiscountRollbackFailureRepository extends ReactiveCrudRepository<DiscountRollbackFailure, Long> {
}
