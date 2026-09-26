package com.feverup.plans.service;

import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.config.ProvidersProperties;
import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.port.EventPersistenceApi;
import com.feverup.plans.domain.port.ProviderFeedApi;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class PlanSyncService {
    
    private final ProviderFeedApi providerFeedApi;
    private final EventPersistenceApi eventPersistenceApi;
    private final ProvidersProperties providersProperties;

    @Scheduled(fixedRateString = "${providers.sync-interval-ms}")
    public void syncScheduled() {
        syncOnce();
    }

    public void syncOnce() {
        for (ProviderDefinition provider : providersProperties.getList()) {
            try {
                List<PlanEvent> records = providerFeedApi.fetchPlanEvents(provider);
                log.info("Fetched {} records from provider {}", records.size(), provider.getId());
                upsertPlanEvents(provider.getId(), records);
            } catch (Exception ex) {
                log.warn("Sync failed for provider {}: {}", provider.getId(), ex.toString());
            }
        }
    }

    @Transactional
    private void upsertPlanEvents(String providerId, List<PlanEvent> planEvents) {
        OffsetDateTime now = OffsetDateTime.now();
        for (PlanEvent incoming : planEvents) {
            saveEventPlan(providerId, incoming, now);
        }
    }

    private void saveEventPlan(String providerId, PlanEvent incoming, OffsetDateTime now) {
        String id = incoming.getId();
        try {
            if (id == null || id.isBlank()) {
                log.warn("Skipping provider [{}] event with missing id", providerId);
                return;
            }

            PlanEvent toSave = eventPersistenceApi
                .findById(id)
                .orElseGet(PlanEvent::new);

            toSave.setId(id);
            toSave.setProviderId(incoming.getProviderId());
            toSave.setExternalId(incoming.getExternalId());
            toSave.setTitle(incoming.getTitle());
            toSave.setStartDate(incoming.getStartDate());
            toSave.setEndDate(incoming.getEndDate());
            toSave.setSellMode(incoming.getSellMode());
            toSave.setSoldOut(incoming.getSoldOut());
            toSave.setLastSeenAt(now);
            toSave.setZones(incoming.getZones());

            boolean online = "online".equalsIgnoreCase(toSave.getSellMode());
            toSave.setEverOnline(toSave.isEverOnline() || online);

            eventPersistenceApi.save(toSave);
            log.info("Event id [{}] from the provider [{}] saved", toSave.getId(), providerId);
        } catch (Exception ex) {
            log.error("Failed to save event id {}: {}", id, ex, ex);
        }
    }
}
