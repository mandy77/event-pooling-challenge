package com.feverup.plans.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.model.ReservationResult;
import com.feverup.plans.domain.model.Zone;
import com.feverup.plans.domain.port.EventPersistenceApi;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.dao.OptimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private EventPersistenceApi eventPersistenceApi;

    @Mock
    private PlatformTransactionManager transactionManager;

    private ReservationService reservationService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        reservationService = new ReservationService(eventPersistenceApi, transactionManager);
        lenient().when(transactionManager.getTransaction(org.mockito.ArgumentMatchers.any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        lenient().doNothing().when(transactionManager).commit(org.mockito.ArgumentMatchers.any());
        lenient().doNothing().when(transactionManager).rollback(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reserveEvent_decrementsZoneCapacity_andPersistsEvent() {
        PlanEvent event = buildEvent("event-1", zone("zone-a", 5), zone("zone-b", 3));
        when(eventPersistenceApi.findByIdForUpdate("event-1")).thenReturn(Optional.of(event));

        ReservationResult result = reservationService.reserveEvent("event-1", "zone-a", 2);

        assertThat(result.remainingCapacity()).isEqualTo(3);
        assertThat(result.soldOut()).isFalse();
        assertThat(event.getZones().get(0).getCapacity()).isEqualTo(3);
        assertThat(event.getSoldOut()).isFalse();
        verify(eventPersistenceApi).save(event);
    }

    @Test
    void reserveEvent_marksEventAsSoldOut_whenLastCapacityIsReserved() {
        PlanEvent event = buildEvent("event-1", zone("zone-a", 1), zone("zone-b", 0));
        when(eventPersistenceApi.findByIdForUpdate("event-1")).thenReturn(Optional.of(event));

        ReservationResult result = reservationService.reserveEvent("event-1", "zone-a", 1);

        assertThat(result.remainingCapacity()).isZero();
        assertThat(result.soldOut()).isTrue();
        assertThat(event.getSoldOut()).isTrue();
        verify(eventPersistenceApi).save(event);
    }

    @Test
    void reserveEvent_throwsWhenEventDoesNotExist() {
        when(eventPersistenceApi.findByIdForUpdate("missing-event")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserveEvent("missing-event", "zone-a", 1))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event not found: missing-event");
    }

    @Test
    void reserveEvent_throwsWhenZoneHasInsufficientCapacity() {
        PlanEvent event = buildEvent("event-1", zone("zone-a", 1));
        when(eventPersistenceApi.findByIdForUpdate("event-1")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> reservationService.reserveEvent("event-1", "zone-a", 2))
                .isInstanceOf(ReservationNotAllowedException.class)
                .hasMessage("Not enough capacity for zone: zone-a");
    }

    @Test
    void reserveEvent_retriesWhenConcurrentUpdateOccurs() {
        PlanEvent staleEvent = buildEvent("event-1", zone("zone-a", 5));
        staleEvent.setVersion(1L);

        PlanEvent freshEvent = buildEvent("event-1", zone("zone-a", 4));
        freshEvent.setVersion(2L);

        when(eventPersistenceApi.findByIdForUpdate("event-1")).thenReturn(Optional.of(staleEvent), Optional.of(freshEvent));
        doThrow(new OptimisticLockingFailureException("conflict")).when(eventPersistenceApi).save(staleEvent);

        ReservationResult result = reservationService.reserveEvent("event-1", "zone-a", 1);

        assertThat(result.remainingCapacity()).isEqualTo(3);
        verify(eventPersistenceApi).save(staleEvent);
        verify(eventPersistenceApi).save(freshEvent);
    }

    private PlanEvent buildEvent(String id, Zone... zones) {
        PlanEvent event = new PlanEvent();
        event.setId(id);
        event.setZones(List.of(zones));
        event.setSoldOut(false);
        return event;
    }

    private Zone zone(String id, int capacity) {
        Zone zone = new Zone();
        zone.setId(id);
        zone.setCapacity(capacity);
        return zone;
    }
}

