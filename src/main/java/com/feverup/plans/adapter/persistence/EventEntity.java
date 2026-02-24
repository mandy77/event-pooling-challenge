package com.feverup.plans.adapter.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "events")
public class EventEntity {
    @Id
    @Column(name = "event_id", nullable = false)
    private String id;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "event_title")
    private String title;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "sell_mode")
    private String sellMode;

    @Column(name = "sold_out")
    private Boolean soldOut;

    @Column(name = "ever_online")
    private boolean everOnline;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_zones", joinColumns = @JoinColumn(name = "event_id"))
    private List<ZoneEmbeddable> zones = new ArrayList<>();

    public EventEntity() {
    }
}
