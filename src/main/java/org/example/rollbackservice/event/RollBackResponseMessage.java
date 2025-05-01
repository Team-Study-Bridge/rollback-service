package org.example.rollbackservice.event;

public record RollBackResponseMessage(
        Long purchaseId,
        boolean isRollBacked,
        String reason
) {}
