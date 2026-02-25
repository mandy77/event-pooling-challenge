package com.feverup.plans.client;

import lombok.Getter;

@Getter
public class ProviderClientException extends RuntimeException {
    private final int statusCode;
    private final boolean retryable;

    public ProviderClientException(String message, int statusCode, boolean retryable) {
        super(message);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }
}
