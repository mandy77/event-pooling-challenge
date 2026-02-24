package com.feverup.plans.service;

import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.config.ProvidersProperties;
import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.port.EventPersistenceApi;
import com.feverup.plans.domain.port.ProviderFeedApi;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class PlanSyncService {
    private static final Logger logger = LoggerFactory.getLogger(PlanSyncService.class);

    private final ProviderFeedApi providerFeedApi;
    private final EventPersistenceApi eventPersistenceApi;
    private final ProvidersProperties providersProperties;

    @Scheduled(fixedRateString = "${providers.sync-interval-ms}")
    public void syncScheduled() {
        syncOnce();
    }

    public void syncOnce() {
        for (ProviderDefinition provider : providersProperties.getList()) {
            providerFeedApi
                    .fetchPlanEvents(provider)
                    .doOnNext(records -> logger.info("Fetched {} records from provider {}", records.size(), provider.getId()))
                    .doOnNext(records -> upsertPlanEvents(provider.getId(), records))
                    .onErrorResume(ex -> {
                        logger.warn("Sync failed for provider {}: {}", provider.getId(), ex.toString());
                        return Mono.empty();
                    })
                    .subscribe();
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
            logger.info("Event id [{}] from the provider [{}] saved", toSave.getId(), providerId);
        } catch (Exception ex) {
            logger.error("Failed to save event id {}: {}", id, ex, ex);
        }
    }
}
