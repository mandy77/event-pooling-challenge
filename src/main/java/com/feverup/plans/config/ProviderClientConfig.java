package com.feverup.plans.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(ProvidersProperties.class)
public class ProviderClientConfig {

    @Bean
    public WebClient.Builder providerWebClientBuilder(ProvidersProperties properties) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs())
            .responseTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
            .doOnConnected(connection -> connection
                .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeoutMs()))
                .addHandlerLast(new WriteTimeoutHandler(properties.getReadTimeoutMs())));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
