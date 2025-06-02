package org.example.rollbackservice.event;

public record RollBackRequestMessage(
        Long purchaseId,
        String impUid,
        Integer paidAmount,
        Long productId
) {}
