package com.feverup.plans.domain.port;

import com.feverup.plans.domain.model.PlanEvent;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface EventPersistenceApi {
    Optional<PlanEvent> findById(String id);

    Optional<PlanEvent> findByIdForUpdate(String id);

    void save(PlanEvent event);

    List<PlanEvent> findByDateRange(OffsetDateTime startsAt, OffsetDateTime endsAt);
}
