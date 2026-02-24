package com.feverup.plans.provider;


import static org.junit.jupiter.api.Assertions.*;

import com.feverup.plans.adapter.provider.ProviderXmlEnvelope;
import com.feverup.plans.adapter.provider.ProviderXmlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProviderXmlParserTest {

    private final static String XML_STRING =
            "<planList xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" xsi:noNamespaceSchemaLocation=\"planList.xsd\">" +
            "<output>" +
            "<base_plan base_plan_id=\"291\" sell_mode=\"online\" title=\"Camela en concierto\">\n" +
            "<plan plan_start_date=\"2021-06-30T21:00:00\" plan_end_date=\"2021-06-30T22:00:00\" plan_id=\"291\" sell_from=\"2020-07-01T00:00:00\" sell_to=\"2021-06-30T20:00:00\" sold_out=\"false\">\n" +
            "<zone zone_id=\"40\" capacity=\"243\" price=\"20.00\" name=\"Platea\" numbered=\"true\"/>\n" +
            "<zone zone_id=\"38\" capacity=\"100\" price=\"15.00\" name=\"Grada 2\" numbered=\"false\"/>\n" +
            "<zone zone_id=\"30\" capacity=\"90\" price=\"30.00\" name=\"A28\" numbered=\"true\"/>\n" +
            "</plan>\n" +
            "</base_plan>\n" +
            "<base_plan base_plan_id=\"322\" sell_mode=\"online\" organizer_company_id=\"2\" title=\"Pantomima Full\">\n" +
            "<plan plan_start_date=\"2021-02-10T20:00:00\" plan_end_date=\"2021-02-10T21:30:00\" plan_id=\"1642\" sell_from=\"2021-01-01T00:00:00\" sell_to=\"2021-02-09T19:50:00\" sold_out=\"false\">\n" +
            "<zone zone_id=\"311\" capacity=\"2\" price=\"55.00\" name=\"A42\" numbered=\"true\"/>\n" +
            "</plan>\n" +
            "<plan plan_start_date=\"2021-02-11T20:00:00\" plan_end_date=\"2021-02-11T21:30:00\" plan_id=\"1643\" sell_from=\"2021-01-01T00:00:00\" sell_to=\"2021-02-10T19:50:00\" sold_out=\"false\">\n" +
            "<zone zone_id=\"311\" capacity=\"2\" price=\"55.00\" name=\"A42\" numbered=\"true\"/>\n" +
            "</plan>\n" +
            "</base_plan>\n" +
            "<base_plan base_plan_id=\"1591\" sell_mode=\"online\" organizer_company_id=\"1\" title=\"Los Morancos\">\n" +
            "<plan plan_start_date=\"2021-07-31T20:00:00\" plan_end_date=\"2021-07-31T21:00:00\" plan_id=\"1642\" sell_from=\"2021-06-26T00:00:00\" sell_to=\"2021-07-31T19:50:00\" sold_out=\"false\">\n" +
            "<zone zone_id=\"186\" capacity=\"2\" price=\"75.00\" name=\"Amfiteatre\" numbered=\"true\"/>\n" +
            "<zone zone_id=\"186\" capacity=\"16\" price=\"65.00\" name=\"Amfiteatre\" numbered=\"false\"/>\n" +
            "</plan>\n" +
            "</base_plan>\n" +
            "</output>\n" +
            "</planList>";

    private ProviderXmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new ProviderXmlParser();
    }

    @Test
    public void testParse() {
        ProviderXmlEnvelope result = parser.parse(XML_STRING);
        assertNotNull(result);
        assertEquals(1, result.getOutputs().size());
        assertEquals(3, result.getOutputs().get(0).getBasePlans().size());
    }
}
