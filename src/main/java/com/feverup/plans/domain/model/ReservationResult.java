package com.feverup.plans.domain.model;

public record ReservationResult(
        String eventId,
        String zoneId,
        int quantity,
        int remainingCapacity,
        boolean soldOut
) {
}

