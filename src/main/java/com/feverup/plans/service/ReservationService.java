package com.feverup.plans.service;

import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.model.ReservationResult;
import com.feverup.plans.domain.model.Zone;
import com.feverup.plans.domain.port.EventPersistenceApi;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ReservationService {

    private static final int MAX_RESERVATION_RETRIES = 5;

    private final EventPersistenceApi eventPersistenceApi;
    private final TransactionTemplate transactionTemplate;

    public ReservationService(EventPersistenceApi eventPersistenceApi, PlatformTransactionManager transactionManager) {
        this.eventPersistenceApi = eventPersistenceApi;
        if (transactionManager == null) {
            this.transactionTemplate = null;
        } else {
            this.transactionTemplate = new TransactionTemplate(transactionManager);
            this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        }
    }

    @CacheEvict(cacheNames = "searchResults", allEntries = true)
    public ReservationResult reserveEvent(String eventId, String zoneId, int quantity) {
        if (transactionTemplate == null) {
            return reserveEventOnce(eventId, zoneId, quantity);
        }

        for (int attempt = 1; attempt <= MAX_RESERVATION_RETRIES; attempt++) {
            try {
                return transactionTemplate.execute(status -> reserveEventOnce(eventId, zoneId, quantity));
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == MAX_RESERVATION_RETRIES) {
                    throw new ReservationNotAllowedException("Reservation could not be completed due to concurrent updates");
                }
            }
        }

        throw new ReservationNotAllowedException("Reservation could not be completed");
    }

    private ReservationResult reserveEventOnce(String eventId, String zoneId, int quantity) {
        PlanEvent event = eventPersistenceApi.findByIdForUpdate(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        List<Zone> zones = event.getZones();
        if (zones == null || zones.isEmpty()) {
            throw new ReservationNotAllowedException("Event has no reservable zones");
        }

        Zone zone = zones.stream()
                .filter(candidate -> zoneId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new ReservationNotAllowedException("Zone not found for event: " + zoneId));

        Integer currentCapacity = zone.getCapacity();
        if (currentCapacity == null) {
            throw new ReservationNotAllowedException("Zone capacity is not available");
        }

        if (currentCapacity < quantity) {
            throw new ReservationNotAllowedException("Not enough capacity for zone: " + zoneId);
        }

        int remainingCapacity = currentCapacity - quantity;
        zone.setCapacity(remainingCapacity);

        boolean soldOut = zones.stream()
                .map(Zone::getCapacity)
                .filter(capacity -> capacity != null && capacity > 0)
                .findAny()
                .isEmpty();

        event.setSoldOut(soldOut);
        eventPersistenceApi.save(event);

        return new ReservationResult(event.getId(), zone.getId(), quantity, remainingCapacity, soldOut);
    }
}

