package com.feverup.plans.adapter.provider;

import com.feverup.plans.client.ProviderClient;
import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.model.Zone;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.feverup.plans.domain.port.ProviderFeedApi;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ProviderFeedAdapter implements ProviderFeedApi {
    private final ProviderClient client;
    private final ProviderXmlParser parser;

    public ProviderFeedAdapter(ProviderClient client, ProviderXmlParser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Mono<List<PlanEvent>> fetchPlanEvents(ProviderDefinition provider) {
        return client.fetchEventsXml(provider)
            .map(parser::parse)
            .map(envelope -> toPlanEvents(provider, envelope));
    }

    private List<PlanEvent> toPlanEvents(ProviderDefinition provider, ProviderXmlEnvelope envelope) {
        List<PlanEvent> events = new ArrayList<>();
        if (envelope == null || envelope.getOutputs() == null) {
            return events;
        }

        for (ProviderXmlOutput output : envelope.getOutputs()) {
            if (output.getBasePlans() == null) {
                continue;
            }
            for (ProviderXmlBasePlan basePlan : output.getBasePlans()) {
                if (basePlan.getPlans() == null) {
                    continue;
                }
                for (ProviderXmlPlan planXml : basePlan.getPlans()) {
                    PlanEvent event = new PlanEvent();
                    String externalId = basePlan.getId() + "-" + planXml.getId();
                    event.setProviderId(provider.getId());
                    event.setExternalId(externalId);
                    event.setId(provider.getId() + ":" + externalId);
                    event.setTitle(basePlan.getTitle());
                    event.setSellMode(basePlan.getSellMode());
                    event.setStartDate(dateOf(planXml.getStartDate()));
                    event.setEndDate(dateOf(planXml.getEndDate()));
                    event.setSoldOut(booleanOf(planXml.getSoldOut()));
                    event.setZones(toZones(planXml.getZones()));
                    events.add(event);
                }
            }
        }

        return events;
    }

    private List<Zone> toZones(List<ProviderXmlZone> zoneXmls) {
        List<Zone> zones = new ArrayList<>();
        if (zoneXmls == null) {
            return zones;
        }
        for (ProviderXmlZone zoneXml : zoneXmls) {
            Zone zone = new Zone();
            zone.setId(zoneXml.getId());
            zone.setName(zoneXml.getName());
            zone.setCapacity(intOf(zoneXml.getCapacity()));
            zone.setPrice(decimalOf(zoneXml.getPrice()));
            zone.setNumbered(booleanOf(zoneXml.getNumbered()));
            zones.add(zone);
        }
        return zones;
    }

    private OffsetDateTime dateOf(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(raw);
        } catch (DateTimeParseException ex) {
            try {
                LocalDateTime local = LocalDateTime.parse(raw);
                return local.atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private Integer intOf(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal decimalOf(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Boolean booleanOf(String raw) {
        if (raw == null) {
            return null;
        }
        return "true".equalsIgnoreCase(raw) || "1".equals(raw);
    }
}
