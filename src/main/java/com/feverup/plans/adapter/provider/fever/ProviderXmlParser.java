package com.feverup.plans.adapter.provider.fever;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProviderXmlParser {

    public ProviderXmlEnvelope parse(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }

        try {
            XmlMapper mapper = new XmlMapper();
            return mapper.readValue(xml, ProviderXmlEnvelope.class);
        } catch (Exception ex) {
            log.warn("Failed to parse provider XML: {}", ex.toString());
            return null;
        }
    }
}
