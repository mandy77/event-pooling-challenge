package com.feverup.plans.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Embeddable
public class ZoneEmbeddable {
    @Column(name = "zone_id")
    private String id;

    @Column(name = "zone_name")
    private String name;

    @Column(name = "zone_price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "zone_capacity")
    private Integer capacity;

    @Column(name = "zone_numbered")
    private Boolean numbered;
}
