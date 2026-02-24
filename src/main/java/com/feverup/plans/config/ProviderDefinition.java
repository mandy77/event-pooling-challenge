package com.feverup.plans.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderDefinition {
    private String id;
    private String baseUrl;
    private String eventsPath;
}
