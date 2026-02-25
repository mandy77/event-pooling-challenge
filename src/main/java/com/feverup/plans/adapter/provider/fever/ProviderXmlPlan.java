package com.feverup.plans.adapter.provider.fever;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderXmlPlan {

    @JacksonXmlProperty(isAttribute = true, localName = "plan_id")
    private String id;

    @JacksonXmlProperty(isAttribute = true, localName = "plan_start_date")
    private String startDate;

    @JacksonXmlProperty(isAttribute = true, localName = "plan_end_date")
    private String endDate;

    @JacksonXmlProperty(isAttribute = true, localName = "sold_out")
    private String soldOut;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "zone")
    private List<ProviderXmlZone> zones = new ArrayList<>();
}
