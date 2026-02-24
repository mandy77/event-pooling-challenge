package com.feverup.plans.adapter.persistence;

import com.feverup.plans.adapter.persistence.repository.EventJpaRepository;
import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.model.Zone;
import com.feverup.plans.domain.port.EventPersistenceApi;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EventPersistenceAdapter implements EventPersistenceApi {
    private final EventJpaRepository repository;

    public EventPersistenceAdapter(EventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PlanEvent> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public void save(PlanEvent event) {
        repository.save(toEntity(event));
    }

    @Override
    public List<PlanEvent> findByDateRange(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        List<EventEntity> entities = repository.findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndEverOnlineTrue(startsAt, endsAt);
        List<PlanEvent> events = new ArrayList<>();
        for (EventEntity entity : entities) {
            events.add(toDomain(entity));
        }
        return events;
    }

    private PlanEvent toDomain(EventEntity entity) {
        PlanEvent event = new PlanEvent();
        event.setId(entity.getId());
        event.setProviderId(entity.getProviderId());
        event.setExternalId(entity.getExternalId());
        event.setTitle(entity.getTitle());
        event.setStartDate(entity.getStartDate());
        event.setEndDate(entity.getEndDate());
        event.setSellMode(entity.getSellMode());
        event.setSoldOut(entity.getSoldOut());
        event.setEverOnline(entity.isEverOnline());
        event.setLastSeenAt(entity.getLastSeenAt());

        List<Zone> zones = new ArrayList<>();
        for (ZoneEmbeddable zoneEmbeddable : entity.getZones()) {
            Zone zone = new Zone();
            zone.setId(zoneEmbeddable.getId());
            zone.setName(zoneEmbeddable.getName());
            zone.setCapacity(zoneEmbeddable.getCapacity());
            zone.setPrice(zoneEmbeddable.getPrice());
            zone.setNumbered(zoneEmbeddable.getNumbered());
            zones.add(zone);
        }
        event.setZones(zones);
        return event;
    }

    private EventEntity toEntity(PlanEvent event) {
        EventEntity entity = new EventEntity();

        entity.setId(event.getId());
        entity.setProviderId(event.getProviderId());
        entity.setExternalId(event.getExternalId());
        entity.setTitle(event.getTitle());
        entity.setStartDate(event.getStartDate());
        entity.setEndDate(event.getEndDate());
        entity.setSellMode(event.getSellMode());
        entity.setSoldOut(event.getSoldOut());
        entity.setEverOnline(event.isEverOnline());
        entity.setLastSeenAt(event.getLastSeenAt());

        List<ZoneEmbeddable> zones = new ArrayList<>();
        if (event.getZones() != null) {
            for (Zone zone : event.getZones()) {
                ZoneEmbeddable zoneEmbeddable = new ZoneEmbeddable();
                zoneEmbeddable.setId(zone.getId());
                zoneEmbeddable.setName(zone.getName());
                zoneEmbeddable.setPrice(zone.getPrice());
                zoneEmbeddable.setCapacity(zone.getCapacity());
                zoneEmbeddable.setNumbered(zone.getNumbered());
                zones.add(zoneEmbeddable);
            }
        }
        entity.getZones().addAll(zones);
        return entity;
    }
}
