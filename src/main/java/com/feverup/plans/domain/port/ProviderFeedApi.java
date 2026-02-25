package com.feverup.plans.domain.port;

import com.feverup.plans.config.ProviderDefinition;
import com.feverup.plans.domain.model.PlanEvent;
import java.util.List;

public interface ProviderFeedApi {

    List<PlanEvent> fetchPlanEvents(ProviderDefinition provider);
}
