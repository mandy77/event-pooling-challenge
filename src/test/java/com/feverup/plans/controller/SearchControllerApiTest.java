package com.feverup.plans.controller;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.model.Zone;
import com.feverup.plans.service.PlanQueryService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanQueryService planQueryService;

    @Test
    void search_returnsBadRequest_whenEndsAtBeforeStartsAt() throws Exception {
        String startsAt = OffsetDateTime.now().toString();
        String endsAt = OffsetDateTime.now().minusDays(1).toString();
        mockMvc.perform(get("/search")
                .param("starts_at", startsAt)
                .param("ends_at", endsAt))
                .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error.code").value(""))
        ;
    }

    @Test
    void search_returnsOkAndEvents_whenValidDates() throws Exception {
        OffsetDateTime startsAt = OffsetDateTime.now();
        OffsetDateTime endsAt = startsAt.plusDays(1);
        PlanEvent event = new PlanEvent();
        event.setId("1");
        event.setTitle("Test Event");
        event.setStartDate(startsAt);
        event.setEndDate(endsAt);
        Zone zone = new Zone();
        zone.setPrice(BigDecimal.TEN);
        event.setZones(List.of(zone));
        when(planQueryService.findEventsCached(startsAt, endsAt)).thenReturn(List.of(event));

        mockMvc.perform(get("/search")
                .param("starts_at", startsAt.toString())
                .param("ends_at", endsAt.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events[0].id").value("1"))
                .andExpect(jsonPath("$.data.events[0].title").value("Test Event"))
                .andExpect(jsonPath("$.data.events[0].min_price").value(10))
                .andExpect(jsonPath("$.data.events[0].max_price").value(10));
    }

    @Test
    void search_returnsOkAndEmptyList_whenNoEvents() throws Exception {
        OffsetDateTime startsAt = OffsetDateTime.now();
        OffsetDateTime endsAt = startsAt.plusDays(1);
        when(planQueryService.findEventsCached(startsAt, endsAt)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/search")
                .param("starts_at", startsAt.toString())
                .param("ends_at", endsAt.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events").isArray())
                .andExpect(jsonPath("$.data.events").isEmpty());
    }

    @Test
    void search_handlesNullZonePrices() throws Exception {
        OffsetDateTime startsAt = OffsetDateTime.now();
        OffsetDateTime endsAt = startsAt.plusDays(1);
        PlanEvent event = new PlanEvent();
        event.setId("1");
        event.setTitle("Test Event");
        event.setStartDate(startsAt);
        event.setEndDate(endsAt);
        event.setZones(List.of(new Zone()));
        when(planQueryService.findEventsCached(startsAt, endsAt)).thenReturn(List.of(event));
        mockMvc.perform(get("/search")
                .param("starts_at", startsAt.toString())
                .param("ends_at", endsAt.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events[0].id").value("1"))
                .andExpect(jsonPath("$.data.events[0].min_price").value(0))
                .andExpect(jsonPath("$.data.events[0].max_price").value(0));
    }
}
