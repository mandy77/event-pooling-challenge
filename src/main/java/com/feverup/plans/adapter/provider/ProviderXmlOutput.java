package com.feverup.plans.adapter.provider;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ProviderXmlOutput {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "base_plan")
    private List<ProviderXmlBasePlan> basePlans = new ArrayList<>();
}
