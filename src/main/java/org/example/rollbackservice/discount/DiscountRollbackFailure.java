package org.example.rollbackservice.discount;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Builder
@Table("discount_rollback_failure")
public record DiscountRollbackFailure(

        @Id Long id,

        @Column("product_id")
        Long productId,

        @Column("purchase_id")
        Long purchaseId,

        String reason,

        @Column("created_at")
        Instant createdAt
) {}
