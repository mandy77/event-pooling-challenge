package com.feverup.plans.client;

import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.config.ProvidersProperties;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ProviderClient {
    private static final Logger logger = LoggerFactory.getLogger(ProviderClient.class);

    private final WebClient.Builder webClientBuilder;
    private final ProvidersProperties properties;

    public ProviderClient(WebClient.Builder providerWebClientBuilder, ProvidersProperties properties) {
        this.webClientBuilder = providerWebClientBuilder;
        this.properties = properties;
    }

    public Mono<String> fetchEventsXml(ProviderDefinition provider) {
        WebClient webClient = webClientBuilder
            .baseUrl(provider.getBaseUrl())
            .build();

        return webClient.get()
                        .uri(provider.getEventsPath())
                        .accept(MediaType.APPLICATION_XML)
                        .exchangeToMono(response ->
                                                handleResponse(response.statusCode(), response.bodyToMono(String.class)))
                        .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                        .doOnError(ex -> logger.warn("Provider fetch failed ({}): {}", provider.getId(), ex.toString()));
    }

    private Mono<String> handleResponse(HttpStatusCode status, Mono<String> bodyMono) {
        return bodyMono.defaultIfEmpty("")
                       .flatMap(body -> {
                           if (status.is2xxSuccessful()) {
                               logger.info("Provider response {} with {} bytes", status.value(), body.length());
                               return Mono.just(body);
                           }
                           logger.warn("Provider response {} with {} bytes", status.value(), body.length());
                           return Mono.error(new IllegalStateException("Provider status " + status.value()));
                       });
    }
}
