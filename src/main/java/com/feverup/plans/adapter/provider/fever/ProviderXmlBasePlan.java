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
public class ProviderXmlBasePlan {

    @JacksonXmlProperty(isAttribute = true, localName = "base_plan_id")
    private String id;

    @JacksonXmlProperty(isAttribute = true, localName = "sell_mode")
    private String sellMode;

    @JacksonXmlProperty(isAttribute = true, localName = "title")
    private String title;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "plan")
    private List<ProviderXmlPlan> plans = new ArrayList<>();
}
