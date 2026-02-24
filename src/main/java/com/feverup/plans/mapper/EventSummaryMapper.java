package com.feverup.plans.mapper;

import com.feverup.plans.domain.model.PlanEvent;
import com.feverup.plans.domain.model.Zone;
import com.feverup.plans.dto.EventSummary;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class EventSummaryMapper {

    public EventSummary toSummary(PlanEvent planEvent) {
        EventSummary event = new EventSummary();
        event.setId(planEvent.getId());
        event.setTitle(planEvent.getTitle());

        if (planEvent.getStartDate() != null) {
            event.setStartDate(planEvent.getStartDate().toLocalDate());
            event.setStartTime(planEvent.getStartDate().toLocalTime());
        }

        if (planEvent.getEndDate() != null) {
            event.setEndDate(planEvent.getEndDate().toLocalDate());
            event.setEndTime(planEvent.getEndDate().toLocalTime());
        }


        List<BigDecimal> prices =
                planEvent.getZones().stream()
                         .map(Zone::getPrice)
                         .filter(Objects::nonNull)
                         .toList();

        BigDecimal minPrice = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        event.setMinPrice(minPrice);
        event.setMaxPrice(maxPrice);
        return event;
    }
}
