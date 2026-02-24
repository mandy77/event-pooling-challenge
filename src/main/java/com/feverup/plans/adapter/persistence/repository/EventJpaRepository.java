package com.feverup.plans.adapter.persistence.repository;

import com.feverup.plans.adapter.persistence.EventEntity;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventJpaRepository extends JpaRepository<EventEntity, String> {
    List<EventEntity> findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndEverOnlineTrue(
        OffsetDateTime startsAt,
        OffsetDateTime endsAt
    );
}
