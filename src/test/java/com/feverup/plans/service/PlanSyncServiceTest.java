package com.feverup.plans.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.config.ProvidersProperties;
import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.port.EventPersistenceApi;
import com.feverup.plans.domain.port.ProviderFeedApi;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanSyncServiceTest {

    @Mock
    private ProviderFeedApi providerFeedApi;

    @Mock
    private EventPersistenceApi eventPersistenceApi;

    @Mock
    private ProvidersProperties providersProperties;

    @InjectMocks
    private PlanSyncService planSyncService;

    @Test
    void syncOnce_savesEventsForConfiguredProviders() {
        ProviderDefinition provider = new ProviderDefinition();
        provider.setId("prov");

        PlanEvent event1 = new PlanEvent();
        event1.setId("prov:ext1");
        event1.setProviderId("prov");
        event1.setExternalId("ext1");

        PlanEvent event2 = new PlanEvent();
        event2.setId("prov:ext2");
        event2.setProviderId("prov");
        event2.setExternalId("ext2");

        when(providersProperties.getList()).thenReturn(List.of(provider));
        when(providerFeedApi.fetchPlanEvents(provider)).thenReturn(List.of(event1, event2));
        when(eventPersistenceApi.findById(any())).thenReturn(Optional.empty());

        planSyncService.syncOnce();

        verify(providerFeedApi).fetchPlanEvents(provider);
        verify(eventPersistenceApi, times(2)).save(any(PlanEvent.class));
    }

    @Test
    void syncOnce_skipsWhenNoProvidersConfigured() {
        when(providersProperties.getList()).thenReturn(List.of());

        planSyncService.syncOnce();

        verifyNoInteractions(providerFeedApi);
        verifyNoInteractions(eventPersistenceApi);
    }

    @Test
    void syncOnce_handlesProviderFailureWithoutThrowing() {
        ProviderDefinition provider = new ProviderDefinition();
        provider.setId("prov");
        when(providersProperties.getList()).thenReturn(List.of(provider));
        when(providerFeedApi.fetchPlanEvents(provider)).thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() -> planSyncService.syncOnce());

        verify(providerFeedApi).fetchPlanEvents(provider);
        verifyNoInteractions(eventPersistenceApi);
    }

    @Test
    void syncOnce_skipsEventsWithNullId() {
        ProviderDefinition provider = new ProviderDefinition();
        provider.setId("prov");

        PlanEvent badEvent = new PlanEvent();
        badEvent.setId(null);

        when(providersProperties.getList()).thenReturn(List.of(provider));
        when(providerFeedApi.fetchPlanEvents(provider)).thenReturn(List.of(badEvent));

        planSyncService.syncOnce();

        verify(providerFeedApi).fetchPlanEvents(provider);
        verifyNoInteractions(eventPersistenceApi);
    }
}
