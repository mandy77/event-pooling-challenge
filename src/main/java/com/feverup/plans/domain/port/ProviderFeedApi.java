package com.feverup.plans.domain.port;

import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.domain.model.PlanEvent;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ProviderFeedApi {
    Mono<List<PlanEvent>> fetchPlanEvents(ProviderDefinition provider);
}
