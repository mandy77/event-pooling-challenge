package com.feverup.plans.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.port.EventPersistenceApi;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EnableCaching
@ActiveProfiles("test")
class PlanQueryServiceTest {
    
    @Autowired
    private PlanQueryService planQueryService;

    @MockBean
    private EventPersistenceApi eventPersistenceApi;

    @Autowired
    private CacheManager cacheManager;

    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private List<PlanEvent> events;

    @BeforeEach
    void setUp() {
        startsAt = OffsetDateTime.now();
        endsAt = startsAt.plusDays(1);
        events = List.of(new PlanEvent());
        // Clear cache before each test
        Objects.requireNonNull(cacheManager.getCache("searchResults")).clear();
    }

    @Test
    void findEventsCached_cachesResults() {
        when(eventPersistenceApi.findByDateRange(startsAt, endsAt)).thenReturn(events);

        // First call: should invoke repository
        List<PlanEvent> result1 = planQueryService.findEventsCached(startsAt, endsAt);
        assertThat(result1).isEqualTo(events);
        verify(eventPersistenceApi, times(1)).findByDateRange(startsAt, endsAt);

        // Second call: should use cache, not invoke repository again
        List<PlanEvent> result2 = planQueryService.findEventsCached(startsAt, endsAt);
        assertThat(result2).isEqualTo(events);
        verify(eventPersistenceApi, times(1)).findByDateRange(startsAt, endsAt);
    }

    @Test
    void findEventsCached_differentParams_notCached() {
        OffsetDateTime otherStart = startsAt.plusDays(2);
        OffsetDateTime otherEnd = endsAt.plusDays(2);
        List<PlanEvent> otherEvents = Collections.emptyList();
        when(eventPersistenceApi.findByDateRange(startsAt, endsAt)).thenReturn(events);
        when(eventPersistenceApi.findByDateRange(otherStart, otherEnd)).thenReturn(otherEvents);

        planQueryService.findEventsCached(startsAt, endsAt);
        planQueryService.findEventsCached(otherStart, otherEnd);

        verify(eventPersistenceApi, times(1)).findByDateRange(startsAt, endsAt);
        verify(eventPersistenceApi, times(1)).findByDateRange(otherStart, otherEnd);
    }
}
