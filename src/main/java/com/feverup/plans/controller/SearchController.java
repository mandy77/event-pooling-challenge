package com.feverup.plans.controller;

import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.dto.EventSummary;
import com.feverup.plans.dto.SearchResponse;
import com.feverup.plans.mapper.EventSummaryMapper;
import com.feverup.plans.service.PlanQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@AllArgsConstructor
@Tag(name = "Search")
public class SearchController {

    private static final String OK = "OK";
    private static final String BAD_REQUEST = "Bad Request (missing required parameters, wrong types...)";

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private final PlanQueryService queryService;
    private final EventSummaryMapper eventSummaryMapper;

    @GetMapping("/search")
    @Operation(summary = "Lists the available events on a time range")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = OK,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SearchResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "data": {
                                                "events": [
                                                  {
                                                    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                                    "title": "string",
                                                    "start_date": "2026-02-23",
                                                    "start_time": "22:38:19",
                                                    "end_date": "2026-02-23",
                                                    "end_time": "14:45:15",
                                                    "min_price": 0,
                                                    "max_price": 0
                                                  }
                                                ]
                                              },
                                              "error": null
                                            }"""
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST, content = {@Content(mediaType = "application/json")}),
    })
    public ResponseEntity<SearchResponse> search(
            @Parameter(description = "Return only events that starts after this date", required = true, example="2017-07-21T17:32:28Z")
            @RequestParam("starts_at") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startsAt,
            @Parameter(description = "Return only events that finishes before this date", required = true, example="2021-07-21T17:32:28Z")
            @RequestParam("ends_at") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endsAt) {

        if (endsAt.isBefore(startsAt)) {
            return ResponseEntity.badRequest()
                                 .body(new SearchResponse(new SearchResponse.Error("", "")));
        }

        List<PlanEvent> events = queryService.findEventsCached(startsAt, endsAt);

        List<EventSummary> responseEvents =
                events.stream()
                      .sorted(Comparator.comparing(PlanEvent::getStartDate))
                      .map(eventSummaryMapper::toSummary)
                      .toList();

        logger.info("event retrieved: {}", responseEvents.size());
        return ResponseEntity.ok(new SearchResponse(responseEvents));
    }
}
