package com.feverup.plans.adapter.provider;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProviderXmlParser {
    private static final Logger logger = LoggerFactory.getLogger(ProviderXmlParser.class);

    public ProviderXmlEnvelope parse(String xml) {
        if (xml == null || xml.isBlank()) {
            return new ProviderXmlEnvelope();
        }

        try {
            XmlMapper mapper = new XmlMapper();
            return mapper.readValue(xml, ProviderXmlEnvelope.class);
        } catch (Exception ex) {
            logger.warn("Failed to parse provider XML: {}", ex.toString());
            return null;
        }
    }
}
