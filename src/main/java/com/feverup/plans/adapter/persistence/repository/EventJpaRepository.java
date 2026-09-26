package com.feverup.plans.adapter.persistence.repository;

import com.feverup.plans.adapter.persistence.EventEntity;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventJpaRepository extends JpaRepository<EventEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct event from EventEntity event left join fetch event.zones where event.id = :id")
    Optional<EventEntity> findByIdForUpdate(@Param("id") String id);

    List<EventEntity> findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndEverOnlineTrue(
        OffsetDateTime startsAt,
        OffsetDateTime endsAt
    );
}
