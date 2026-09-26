package com.feverup.plans.service;

public class ReservationNotAllowedException extends RuntimeException {
    public ReservationNotAllowedException(String message) {
        super(message);
    }
}

