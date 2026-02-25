package com.feverup.plans.adapter.provider.fever;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProviderXmlParserTest {

    private static final String XML_STRING =
        "<planList xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" xsi:noNamespaceSchemaLocation=\"planList.xsd\">" +
        "<output>" +
        "<base_plan base_plan_id=\"291\" sell_mode=\"online\" title=\"Camela en concierto\">" +
        "<plan plan_start_date=\"2021-06-30T21:00:00\" plan_end_date=\"2021-06-30T22:00:00\" plan_id=\"291\" sell_from=\"2020-07-01T00:00:00\" sell_to=\"2021-06-30T20:00:00\" sold_out=\"false\">" +
        "<zone zone_id=\"40\" capacity=\"243\" price=\"20.00\" name=\"Platea\" numbered=\"true\"/>" +
        "</plan>" +
        "</base_plan>" +
        "<base_plan base_plan_id=\"322\" sell_mode=\"online\" organizer_company_id=\"2\" title=\"Pantomima Full\">" +
        "<plan plan_start_date=\"2021-02-10T20:00:00\" plan_end_date=\"2021-02-10T21:30:00\" plan_id=\"1642\" sell_from=\"2021-01-01T00:00:00\" sell_to=\"2021-02-09T19:50:00\" sold_out=\"false\">" +
        "<zone zone_id=\"311\" capacity=\"2\" price=\"55.00\" name=\"A42\" numbered=\"true\"/>" +
        "</plan>" +
        "<plan plan_start_date=\"2021-02-11T20:00:00\" plan_end_date=\"2021-02-11T21:30:00\" plan_id=\"1643\" sell_from=\"2021-01-01T00:00:00\" sell_to=\"2021-02-10T19:50:00\" sold_out=\"false\">" +
        "<zone zone_id=\"311\" capacity=\"2\" price=\"55.00\" name=\"A42\" numbered=\"true\"/>" +
        "</plan>" +
        "</base_plan>" +
        "</output>" +
        "</planList>";

    private static final String XML_WITH_UNKNOWN_ATTRS =
        "<planList xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" xsi:noNamespaceSchemaLocation=\"planList.xsd\" extra=\"x\">" +
        "<output>" +
        "<base_plan base_plan_id=\"1\" sell_mode=\"online\" title=\"Test\" extra_attr=\"x\">" +
        "<plan plan_start_date=\"2021-06-30T21:00:00\" plan_end_date=\"2021-06-30T22:00:00\" plan_id=\"2\" sold_out=\"false\" unknown=\"y\">" +
        "</plan>" +
        "</base_plan>" +
        "</output>" +
        "</planList>";

    private static final String XML_EMPTY = "<planList></planList>";
    private static final String XML_MALFORMED = "<planList><output>";

    private ProviderXmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new ProviderXmlParser();
    }

    @Test
    void parse_validXml_parsesOutputsAndBasePlans() {
        ProviderXmlEnvelope result = parser.parse(XML_STRING);

        assertNotNull(result);
        assertNotNull(result.getOutputs());
        assertEquals(1, result.getOutputs().size());
        assertEquals(2, result.getOutputs().get(0).getBasePlans().size());
        assertEquals("291", result.getOutputs().get(0).getBasePlans().get(0).getId());
        assertEquals("Camela en concierto", result.getOutputs().get(0).getBasePlans().get(0).getTitle());
        assertEquals(1, result.getOutputs().get(0).getBasePlans().get(0).getPlans().size());
        assertEquals("291", result.getOutputs().get(0).getBasePlans().get(0).getPlans().get(0).getId());
    }

    @Test
    void parse_handlesUnknownAttributes() {
        ProviderXmlEnvelope result = parser.parse(XML_WITH_UNKNOWN_ATTRS);

        assertNotNull(result);
        assertEquals(1, result.getOutputs().size());
        assertEquals(1, result.getOutputs().get(0).getBasePlans().size());
        assertEquals("1", result.getOutputs().get(0).getBasePlans().get(0).getId());
        assertEquals(1, result.getOutputs().get(0).getBasePlans().get(0).getPlans().size());
        assertEquals("2", result.getOutputs().get(0).getBasePlans().get(0).getPlans().get(0).getId());
    }

    @Test
    void parse_emptyXml_returnsEnvelopeWithEmptyOutputsOrNull() {
        ProviderXmlEnvelope result = parser.parse(XML_EMPTY);
        if (result == null) {
            return;
        }
        List<ProviderXmlOutput> outputs = result.getOutputs();
        if (outputs != null) {
            assertTrue(outputs.isEmpty());
        }
    }

    @Test
    void parse_malformedXml_returnsNull() {
        ProviderXmlEnvelope result = parser.parse(XML_MALFORMED);
        assertNull(result);
    }
}
