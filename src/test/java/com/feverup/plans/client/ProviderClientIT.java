package com.feverup.plans.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.feverup.plans.config.ProviderClientConfig;
import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.config.ProvidersProperties;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

class ProviderClientIT {

    private DisposableServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = null;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @Test
    void fetchEventsXml_returnsBodyFromProvider() {
        server = HttpServer.create()
            .port(0)
            .route(routes -> routes.get("/api/events", (request, response) ->
                response.status(200)
                    .header("Content-Type", "application/xml")
                    .sendString(Mono.just("<planList></planList>"))
            ))
            .bindNow(Duration.ofSeconds(5));

        baseUrl = "http://localhost:" + server.port();

        ProvidersProperties properties = new ProvidersProperties();
        properties.setConnectTimeoutMs(2000);
        properties.setReadTimeoutMs(2000);

        ProviderDefinition provider = new ProviderDefinition();
        provider.setId("test");
        provider.setBaseUrl(baseUrl);
        provider.setEventsPath("/api/events");

        ProviderClientConfig config = new ProviderClientConfig();
        ProviderClient client = new ProviderClient(config.providerWebClientBuilder(properties), properties);

        String body = client.fetchEventsXml(provider).block(Duration.ofSeconds(2));

        assertNotNull(body);
        assertEquals("<planList></planList>", body);
    }

    @Test
    void fetchEventsXml_throwsOnNon2xx() {
        server = HttpServer.create()
            .port(0)
            .route(routes -> routes.get("/api/events", (request, response) ->
                response.status(500).send()
            ))
            .bindNow(Duration.ofSeconds(5));

        baseUrl = "http://localhost:" + server.port();

        ProvidersProperties properties = new ProvidersProperties();
        properties.setConnectTimeoutMs(2000);
        properties.setReadTimeoutMs(2000);

        ProviderDefinition provider = new ProviderDefinition();
        provider.setId("test");
        provider.setBaseUrl(baseUrl);
        provider.setEventsPath("/api/events");

        ProviderClientConfig config = new ProviderClientConfig();
        ProviderClient client = new ProviderClient(config.providerWebClientBuilder(properties), properties);

        assertThrows(Exception.class, () -> client.fetchEventsXml(provider).block(Duration.ofSeconds(2)));
    }

    @Test
    void fetchEventsXml_timesOutOnSlowProvider() {
        server = HttpServer.create()
            .port(0)
            .route(routes -> routes.get("/api/events", (request, response) ->
                response.sendString(Mono.just("<planList></planList>").delayElement(Duration.ofSeconds(3)))
            ))
            .bindNow(Duration.ofSeconds(5));

        baseUrl = "http://localhost:" + server.port();

        ProvidersProperties properties = new ProvidersProperties();
        properties.setConnectTimeoutMs(2000);
        properties.setReadTimeoutMs(2000);

        ProviderDefinition provider = new ProviderDefinition();
        provider.setId("test");
        provider.setBaseUrl(baseUrl);
        provider.setEventsPath("/api/events");

        ProviderClientConfig config = new ProviderClientConfig();
        ProviderClient client = new ProviderClient(config.providerWebClientBuilder(properties), properties);

        assertThrows(Exception.class, () -> client.fetchEventsXml(provider).block(Duration.ofSeconds(2)));
    }
}
