package com.feverup.plans.client;

import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.config.ProvidersProperties;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProviderClient {
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
                        .doOnError(ex -> log.warn("Provider fetch failed ({}): {}", provider.getId(), ex.toString()));
    }

    private Mono<String> handleResponse(HttpStatusCode status, Mono<String> bodyMono) {
        return bodyMono.defaultIfEmpty("")
                       .flatMap(body -> {
                           if (status.is2xxSuccessful()) {
                               log.info("Provider response {} with {} bytes", status.value(), body.length());
                               return Mono.just(body);
                           }
                           log.warn("Provider response {} with {} bytes", status.value(), body.length());
                           return Mono.error(new IllegalStateException("Provider status " + status.value()));
                       });
    }
}
