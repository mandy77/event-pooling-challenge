package com.feverup.plans.domain.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlanEvent {
    private String id;
    private Long version;
    private String providerId;
    private String externalId;
    private String title;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String sellMode;
    private Boolean soldOut;
    private boolean everOnline;
    private OffsetDateTime lastSeenAt;
    private List<Zone> zones = new ArrayList<>();
}
