package com.feverup.plans.controller;

import com.feverup.plans.domain.model.ReservationResult;
import com.feverup.plans.dto.ReservationResponse;
import com.feverup.plans.dto.ReserveEventRequest;
import com.feverup.plans.service.EventNotFoundException;
import com.feverup.plans.service.ReservationNotAllowedException;
import com.feverup.plans.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/events")
@Tag(name = "Reservations")
public class ReservationController {

    static final String NOT_FOUND_CODE = "404";
    static final String CONFLICT_CODE = "409";
    static final String INTERNAL_ERROR_CODE = "500";

    private final ReservationService reservationService;

    @PostMapping("/{eventId}/reservations")
    @Operation(summary = "Reserve capacity for an event zone")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = """
                                            {
                                              "data": {
                                                "event_id": "fever:322-1643",
                                                "zone_id": "40",
                                                "quantity": 2,
                                                "remaining_capacity": 48,
                                                "sold_out": false
                                              },
                                              "error": null
                                            }"""
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Reservation cannot be completed", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ReservationResponse> reserveEvent(
            @Parameter(description = "Event identifier", required = true)
            @PathVariable("eventId") String eventId,
            @Valid @RequestBody ReserveEventRequest request) {
        try {
            ReservationResult result = reservationService.reserveEvent(eventId, request.zoneId(), request.quantity());
            log.info("Reserved {} seats for event {} in zone {}", request.quantity(), eventId, request.zoneId());
            return ResponseEntity.status(HttpStatus.CREATED).body(new ReservationResponse(result));
        } catch (EventNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ReservationResponse(new ReservationResponse.Error(NOT_FOUND_CODE, ex.getMessage())));
        } catch (ReservationNotAllowedException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ReservationResponse(new ReservationResponse.Error(CONFLICT_CODE, ex.getMessage())));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ReservationResponse(new ReservationResponse.Error(INTERNAL_ERROR_CODE, ex.getMessage())));
        }
    }
}

