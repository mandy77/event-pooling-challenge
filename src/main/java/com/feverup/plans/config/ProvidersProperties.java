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
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 10000;
    private long syncIntervalMs = 60000;
    private List<ProviderDefinition> list = new ArrayList<>();
}
