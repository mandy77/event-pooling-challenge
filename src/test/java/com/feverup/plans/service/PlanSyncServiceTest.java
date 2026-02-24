package com.feverup.plans.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.port.EventPersistenceApi;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PlanSyncServiceTest {
    @Mock
    private EventPersistenceApi eventPersistenceApi;

    private PlanSyncService planSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        planSyncService = new PlanSyncService(null, eventPersistenceApi, null);
    }

    @Test
    void upsertPlanEvents_savesAllEvents() throws Exception {
        PlanEvent event1 = new PlanEvent();
        event1.setId("1");
        event1.setProviderId("prov");
        event1.setExternalId("ext1");
        PlanEvent event2 = new PlanEvent();
        event2.setId("2");
        event2.setProviderId("prov");
        event2.setExternalId("ext2");

        when(eventPersistenceApi.findById(any())).thenReturn(Optional.empty());

        Method method = PlanSyncService.class.getDeclaredMethod("upsertPlanEvents", String.class, List.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(planSyncService, "prov", List.of(event1, event2)));
        verify(eventPersistenceApi, times(2)).save(any(PlanEvent.class));
    }

    @Test
    void upsertPlanEvents_continuesOnException() throws Exception {
        PlanEvent event1 = new PlanEvent();
        event1.setId("1");
        event1.setProviderId("prov");
        event1.setExternalId("ext1");
        PlanEvent event2 = new PlanEvent();
        event2.setId("2");
        event2.setProviderId("prov");
        event2.setExternalId("ext2");
        when(eventPersistenceApi.findById(any())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("DB error")).when(eventPersistenceApi).save(argThat(e -> "1".equals(e.getId())));

        Method method = PlanSyncService.class.getDeclaredMethod("upsertPlanEvents", String.class, List.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(planSyncService, "prov", List.of(event1, event2)));
        verify(eventPersistenceApi, times(2)).save(any(PlanEvent.class));
    }
}
