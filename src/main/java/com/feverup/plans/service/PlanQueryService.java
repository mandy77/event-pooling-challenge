package com.feverup.plans.service;

import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.port.EventPersistenceApi;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PlanQueryService {

    private final EventPersistenceApi eventPersistenceApi;

    @Cacheable(cacheNames = "searchResults", unless = "#result == null || #result.isEmpty()")
    public List<PlanEvent> findEventsCached(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        return findEvents(startsAt, endsAt);
    }

    private List<PlanEvent> findEvents(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        return eventPersistenceApi.findByDateRange(startsAt, endsAt);
    }
}
