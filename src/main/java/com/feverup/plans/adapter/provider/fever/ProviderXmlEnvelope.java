package com.feverup.plans.adapter.provider.fever;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@JacksonXmlRootElement(localName = "planList")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderXmlEnvelope {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "output")
    private List<ProviderXmlOutput> outputs = new ArrayList<>();
}
