package com.feverup.plans.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "providers")
public class ProvidersProperties {
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 10000;
    private long syncIntervalMs = 30000;

    private int retryMaxAttempts = 3;
    private long retryInitialBackoffMs = 200;
    private long retryMaxBackoffMs = 2000;
    private List<ProviderDefinition> list = new ArrayList<>();
}
