package com.feverup.plans.dto;

import java.util.List;

public record SearchResponse(SearchData data, Error error) {

    public SearchResponse(List<EventSummary> events) {
        this(new SearchData(events), null);
    }

    public SearchResponse(Error error) {
        this(null, error);
    }

    public record SearchData(List<EventSummary> events) {}


    public record Error(String code, String title) {}
}
