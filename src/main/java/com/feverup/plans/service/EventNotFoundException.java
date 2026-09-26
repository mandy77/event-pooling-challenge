package com.feverup.plans.service;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String eventId) {
        super("Event not found: " + eventId);
    }
}

