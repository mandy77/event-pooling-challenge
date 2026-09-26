package com.feverup.plans.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.feverup.plans.adapter.persistence.EventEntity;
import com.feverup.plans.adapter.persistence.ZoneEmbeddable;
import com.feverup.plans.adapter.persistence.repository.EventJpaRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:reservation-concurrency-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.cache.type=simple",
        "providers.sync-interval-ms=999999999"
})
class ReservationServiceConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        eventJpaRepository.deleteAll();
        executorService = Executors.newFixedThreadPool(6);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void reserveEvent_allowsOnlyAvailableCapacity_underConcurrentRequests() throws Exception {
        eventJpaRepository.saveAndFlush(eventEntity("event-1", zone("zone-a", 3)));

        int concurrentRequests = 6;
        CountDownLatch ready = new CountDownLatch(concurrentRequests);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Boolean>> futures = IntStream.range(0, concurrentRequests)
                .mapToObj(index -> executorService.submit(() -> attemptReservation(ready, start)))
                .toList();

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();

        int successfulReservations = 0;
        int rejectedReservations = 0;
        for (Future<Boolean> future : futures) {
            if (future.get(10, TimeUnit.SECONDS)) {
                successfulReservations++;
            } else {
                rejectedReservations++;
            }
        }

        EventEntity storedEvent = eventJpaRepository.findById("event-1").orElseThrow();
        ZoneEmbeddable storedZone = storedEvent.getZones().get(0);

        assertThat(successfulReservations).isEqualTo(3);
        assertThat(rejectedReservations).isEqualTo(3);
        assertThat(storedZone.getCapacity()).isZero();
        assertThat(storedEvent.getSoldOut()).isTrue();
    }

    private boolean attemptReservation(CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown();
        assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
        try {
            reservationService.reserveEvent("event-1", "zone-a", 1);
            return true;
        } catch (ReservationNotAllowedException ex) {
            return false;
        }
    }

    private EventEntity eventEntity(String id, ZoneEmbeddable... zones) {
        EventEntity entity = new EventEntity();
        entity.setId(id);
        entity.setProviderId("fever");
        entity.setExternalId(id);
        entity.setTitle("Concurrent Event");
        entity.setStartDate(OffsetDateTime.parse("2026-09-22T18:00:00Z"));
        entity.setEndDate(OffsetDateTime.parse("2026-09-22T20:00:00Z"));
        entity.setSellMode("online");
        entity.setSoldOut(false);
        entity.setEverOnline(true);
        entity.setLastSeenAt(OffsetDateTime.parse("2026-09-22T10:00:00Z"));
        entity.setZones(new ArrayList<>(List.of(zones)));
        return entity;
    }

    private ZoneEmbeddable zone(String id, int capacity) {
        ZoneEmbeddable zone = new ZoneEmbeddable();
        zone.setId(id);
        zone.setName(id);
        zone.setCapacity(capacity);
        zone.setNumbered(false);
        return zone;
    }
}

