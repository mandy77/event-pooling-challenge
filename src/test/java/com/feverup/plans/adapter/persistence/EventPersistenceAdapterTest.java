package com.feverup.plans.adapter.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.feverup.plans.adapter.persistence.repository.EventJpaRepository;
import com.feverup.plans.domain.model.PlanEvent;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventPersistenceAdapterTest {
    @Mock
    private EventJpaRepository repository;

    @InjectMocks
    private EventPersistenceAdapter adapter;

    @Test
    void findById_returnsPlanEvent_whenEntityExists() {
        EventEntity entity = new EventEntity();
        entity.setId("1");
        entity.setTitle("Test Event");
        entity.setStartDate(OffsetDateTime.now());
        entity.setEndDate(OffsetDateTime.now().plusHours(2));
        entity.setEverOnline(true);
        entity.setZones(Collections.emptyList());
        when(repository.findById("1")).thenReturn(Optional.of(entity));

        Optional<PlanEvent> result = adapter.findById("1");
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        assertEquals("Test Event", result.get().getTitle());
    }

    @Test
    void findById_returnsEmpty_whenEntityDoesNotExist() {
        when(repository.findById("2")).thenReturn(Optional.empty());
        Optional<PlanEvent> result = adapter.findById("2");
        assertFalse(result.isPresent());
    }

    @Test
    void save_callsRepositorySave_withConvertedEntity() {
        PlanEvent event = new PlanEvent();
        event.setId("3");
        event.setTitle("Save Event");
        event.setStartDate(OffsetDateTime.now());
        event.setEndDate(OffsetDateTime.now().plusHours(1));
        event.setEverOnline(true);
        event.setZones(Collections.emptyList());

        adapter.save(event);
        verify(repository, times(1)).save(any(EventEntity.class));
    }

    @Test
    void findByDateRange_returnsPlanEvents() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusDays(1);
        EventEntity entity = new EventEntity();
        entity.setId("4");
        entity.setTitle("Range Event");
        entity.setStartDate(start);
        entity.setEndDate(end);
        entity.setEverOnline(true);
        entity.setZones(Collections.emptyList());
        when(repository.findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndEverOnlineTrue(start, end))
                .thenReturn(Collections.singletonList(entity));

        List<PlanEvent> result = adapter.findByDateRange(start, end);
        assertEquals(1, result.size());
        assertEquals("4", result.get(0).getId());
        assertEquals("Range Event", result.get(0).getTitle());
    }

    @Test
    void findByDateRange_returnsEmptyList_whenNoEntities() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusDays(1);
        when(repository.findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndEverOnlineTrue(start, end))
                .thenReturn(Collections.emptyList());
        List<PlanEvent> result = adapter.findByDateRange(start, end);
        assertTrue(result.isEmpty());
    }
}
