package com.feverup.plans.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feverup.plans.domain.model.ReservationResult;

public record ReservationResponse(ReservationData data, Error error) {

    public ReservationResponse(ReservationResult result) {
        this(new ReservationData(
                result.eventId(),
                result.zoneId(),
                result.quantity(),
                result.remainingCapacity(),
                result.soldOut()
        ), null);
    }

    public ReservationResponse(Error error) {
        this(null, error);
    }

    public record ReservationData(
            @JsonProperty("event_id")
            String eventId,
            @JsonProperty("zone_id")
            String zoneId,
            int quantity,
            @JsonProperty("remaining_capacity")
            int remainingCapacity,
            @JsonProperty("sold_out")
            boolean soldOut
    ) {
    }

    public record Error(String code, String title) {
    }
}

