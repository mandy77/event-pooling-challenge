package com.feverup.plans.adapter.provider.fever;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.feverup.plans.client.ProviderClient;
import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.domain.model.PlanEvent;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ProviderFeedAdapterTest {

    @Mock
    private ProviderClient client;

    @Mock
    private ProviderXmlParser parser;

    @InjectMocks
    private ProviderFeedAdapter adapter;

    @Test
    void fetchPlanEvents_mapsEnvelopeToDomain() {
        ProviderDefinition provider = new ProviderDefinition();
        provider.setId("prov");

        ProviderXmlEnvelope envelope = new ProviderXmlEnvelope();
        ProviderXmlOutput output = new ProviderXmlOutput();
        ProviderXmlBasePlan basePlan = new ProviderXmlBasePlan();
        basePlan.setId("b1");
        basePlan.setTitle("title");
        basePlan.setSellMode("online");

        ProviderXmlPlan plan = new ProviderXmlPlan();
        plan.setId("p1");
        plan.setStartDate(OffsetDateTime.parse("2026-02-24T10:00:00Z").toString());
        plan.setEndDate(OffsetDateTime.parse("2026-02-24T11:00:00Z").toString());
        plan.setSoldOut("false");

        ProviderXmlZone zone = new ProviderXmlZone();
        zone.setId("z1");
        zone.setName("zone1");
        zone.setCapacity("100");
        zone.setPrice("10.5");
        zone.setNumbered("true");

        plan.setZones(List.of(zone));
        basePlan.setPlans(List.of(plan));
        output.setBasePlans(List.of(basePlan));
        envelope.setOutputs(List.of(output));

        when(client.fetchEventsXml(any())).thenReturn(Mono.just("xml"));
        when(parser.parse("xml")).thenReturn(envelope);

        List<PlanEvent> events = adapter.fetchPlanEvents(provider);

        assertNotNull(events);
        assertEquals(1, events.size());
        PlanEvent event = events.get(0);
        assertEquals("prov", event.getProviderId());
        assertEquals("b1-p1", event.getExternalId());
        assertEquals("prov:b1-p1", event.getId());
        assertEquals("title", event.getTitle());
        assertEquals("online", event.getSellMode());
        assertNotNull(event.getStartDate());
        assertNotNull(event.getEndDate());
        assertFalse(event.getSoldOut());
        assertEquals(1, event.getZones().size());
        assertEquals("z1", event.getZones().get(0).getId());
        assertEquals("zone1", event.getZones().get(0).getName());
        assertEquals(100, event.getZones().get(0).getCapacity());
        assertEquals(new BigDecimal("10.5"), event.getZones().get(0).getPrice());
        assertTrue(event.getZones().get(0).getNumbered());

        verify(client).fetchEventsXml(provider);
        verify(parser).parse("xml");
    }

    @Test
    void fetchPlanEvents_returnsEmpty_whenEnvelopeIsNull() {
        ProviderDefinition provider = new ProviderDefinition();
        when(client.fetchEventsXml(any())).thenReturn(Mono.just("xml"));
        when(parser.parse("xml")).thenReturn(null);

        List<PlanEvent> events = adapter.fetchPlanEvents(provider);

        assertNotNull(events);
        assertTrue(events.isEmpty());
        verify(client).fetchEventsXml(provider);
        verify(parser).parse("xml");
    }

    @Test
    void fetchPlanEvents_returnsEmpty_whenOutputsNull() {
        ProviderDefinition provider = new ProviderDefinition();
        ProviderXmlEnvelope envelope = new ProviderXmlEnvelope();
        envelope.setOutputs(null);

        when(client.fetchEventsXml(any())).thenReturn(Mono.just("xml"));
        when(parser.parse("xml")).thenReturn(envelope);

        List<PlanEvent> events = adapter.fetchPlanEvents(provider);

        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void fetchPlanEvents_handlesInvalidFields() {
        ProviderDefinition provider = new ProviderDefinition();
        provider.setId("prov");

        ProviderXmlEnvelope envelope = new ProviderXmlEnvelope();
        ProviderXmlOutput output = new ProviderXmlOutput();
        ProviderXmlBasePlan basePlan = new ProviderXmlBasePlan();
        basePlan.setId("b1");
        basePlan.setTitle("title");
        basePlan.setSellMode("online");

        ProviderXmlPlan plan = new ProviderXmlPlan();
        plan.setId("p1");
        plan.setStartDate("notadate");
        plan.setEndDate(null);
        plan.setSoldOut("notabool");

        ProviderXmlZone zone = new ProviderXmlZone();
        zone.setId("z1");
        zone.setName("zone1");
        zone.setCapacity("notanint");
        zone.setPrice("notanum");
        zone.setNumbered("notabool");

        plan.setZones(List.of(zone));
        basePlan.setPlans(List.of(plan));
        output.setBasePlans(List.of(basePlan));
        envelope.setOutputs(List.of(output));

        when(client.fetchEventsXml(any())).thenReturn(Mono.just("xml"));
        when(parser.parse("xml")).thenReturn(envelope);

        List<PlanEvent> events = adapter.fetchPlanEvents(provider);

        assertNotNull(events);
        assertEquals(1, events.size());
        PlanEvent event = events.get(0);
        assertNull(event.getStartDate());
        assertNull(event.getEndDate());
        assertEquals(1, event.getZones().size());
        assertNull(event.getZones().get(0).getCapacity());
        assertNull(event.getZones().get(0).getPrice());
        assertFalse(event.getZones().get(0).getNumbered());
        assertFalse(event.getSoldOut());
    }
}
