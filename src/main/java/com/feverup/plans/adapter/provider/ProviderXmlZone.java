package com.feverup.plans.adapter.provider;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProviderXmlZone {
    @Setter
    @JacksonXmlProperty(isAttribute = true, localName = "zone_id")
    private String id;

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "price")
    private String price;

    @JacksonXmlProperty(isAttribute = true, localName = "capacity")
    private String capacity;

    @JacksonXmlProperty(isAttribute = true, localName = "numbered")
    private String numbered;
}
