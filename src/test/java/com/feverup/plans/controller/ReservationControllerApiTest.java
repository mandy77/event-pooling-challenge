package com.feverup.plans.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.feverup.plans.adapter.persistence.EventEntity;
import com.feverup.plans.adapter.persistence.ZoneEmbeddable;
import com.feverup.plans.adapter.persistence.repository.EventJpaRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:reservation-controller-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.cache.type=simple",
        "providers.sync-interval-ms=999999999"
})
@AutoConfigureMockMvc
class ReservationControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @BeforeEach
    void setUp() {
        eventJpaRepository.deleteAll();
    }

    @Test
    void reserveEvent_returnsCreated_whenReservationSucceeds() throws Exception {
        EventEntity event = eventEntity("event-1", false, zone("zone-a", 5), zone("zone-b", 2));
        eventJpaRepository.saveAndFlush(event);

        mockMvc.perform(post("/events/{eventId}/reservations", "event-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "zone_id": "zone-a",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.event_id").value("event-1"))
                .andExpect(jsonPath("$.data.zone_id").value("zone-a"))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.remaining_capacity").value(3))
                .andExpect(jsonPath("$.data.sold_out").value(false))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void reserveEvent_returnsNotFound_whenEventDoesNotExist() throws Exception {
        mockMvc.perform(post("/events/{eventId}/reservations", "missing-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "zone_id": "zone-a",
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value(ReservationController.NOT_FOUND_CODE));
    }

    @Test
    void reserveEvent_returnsConflict_whenRequestedCapacityIsNotAvailable() throws Exception {
        EventEntity event = eventEntity("event-1", false, zone("zone-a", 1));
        eventJpaRepository.saveAndFlush(event);

        mockMvc.perform(post("/events/{eventId}/reservations", "event-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "zone_id": "zone-a",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value(ReservationController.CONFLICT_CODE));
    }

    @Test
    void reserveEvent_returnsBadRequest_whenRequestIsInvalid() throws Exception {
        EventEntity event = eventEntity("event-1", false, zone("zone-a", 5));
        eventJpaRepository.saveAndFlush(event);

        mockMvc.perform(post("/events/{eventId}/reservations", "event-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "zone_id": "",
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private EventEntity eventEntity(String id, boolean soldOut, ZoneEmbeddable... zones) {
        EventEntity entity = new EventEntity();
        entity.setId(id);
        entity.setProviderId("fever");
        entity.setExternalId(id);
        entity.setTitle("Sample Event");
        entity.setStartDate(OffsetDateTime.parse("2026-09-22T18:00:00Z"));
        entity.setEndDate(OffsetDateTime.parse("2026-09-22T20:00:00Z"));
        entity.setSellMode("online");
        entity.setSoldOut(soldOut);
        entity.setEverOnline(true);
        entity.setLastSeenAt(OffsetDateTime.parse("2026-09-22T10:00:00Z"));
        entity.setZones(new ArrayList<>(List.of(zones)));
        return entity;
    }

    private ZoneEmbeddable zone(String id, int capacity) {
        ZoneEmbeddable zone = new ZoneEmbeddable();
        zone.setId(id);
        zone.setName(id);
        zone.setCapacity(capacity);
        zone.setNumbered(false);
        return zone;
    }
}

