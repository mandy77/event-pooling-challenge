package com.feverup.plans.domain.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Zone {
    private String id;
    private String name;
    private BigDecimal price;
    private Integer capacity;
    private Boolean numbered;
}
