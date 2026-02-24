package com.feverup.plans.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
public class EventSummary {
    @Schema(description = "Event identifier")
    private String id;
    @Schema(description = "Event title")
    private String title;

    @JsonProperty("start_date")
    @Schema(description = "Event start date (YYYY-MM-DD)")
    private LocalDate startDate;

    @JsonProperty("start_time")
    @Schema(description = "Event start time (HH:mm:ss)")
    private LocalTime startTime;

    @JsonProperty("end_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Event end date (YYYY-MM-DD)")
    private LocalDate endDate;

    @JsonProperty("end_time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Schema(description = "Event end time (HH:mm:ss)")
    private LocalTime endTime;

    @JsonProperty("min_price")
    @Schema(description = "Minimum zone price for the event")
    private BigDecimal minPrice;

    @JsonProperty("max_price")
    @Schema(description = "Maximum zone price for the event")
    private BigDecimal maxPrice;
}
