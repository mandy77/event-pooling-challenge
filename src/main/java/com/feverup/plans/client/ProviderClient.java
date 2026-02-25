package com.feverup.plans.client;

import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.config.ProvidersProperties;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
                        .retryWhen(Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryInitialBackoffMs()))
                            .maxBackoff(Duration.ofMillis(properties.getRetryMaxBackoffMs()))
                            .filter(this::isRetryable))
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
                           boolean retryable = status.is5xxServerError();
                           return Mono.error(new ProviderClientException("Provider status " + status.value(), status.value(), retryable));
                       });
    }

    private boolean isRetryable(Throwable ex) {
        if (ex instanceof ProviderClientException providerEx) {
            return providerEx.isRetryable();
        }
        return ex instanceof TimeoutException || ex instanceof WebClientRequestException;
    }

}