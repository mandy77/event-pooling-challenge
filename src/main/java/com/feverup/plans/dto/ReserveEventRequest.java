package com.feverup.plans.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ReserveEventRequest(
        @JsonProperty("zone_id")
        @NotBlank(message = "zone_id is required")
        String zoneId,
        @Positive(message = "quantity must be greater than zero")
        int quantity
) {
}

