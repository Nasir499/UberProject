package com.example.uberentityservice.models;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum BookingStatus {
    REQUESTED,
    DRIVER_ASSIGNED,
    DRIVER_ACCEPTED,
    DRIVER_ARRIVING,
    RIDE_STARTED,
    IN_RIDE,
    COMPLETED,
    CANCELLED;

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = Map.of(
            REQUESTED, EnumSet.of(DRIVER_ASSIGNED, DRIVER_ACCEPTED, CANCELLED),
            DRIVER_ASSIGNED, EnumSet.of(DRIVER_ASSIGNED, DRIVER_ACCEPTED, DRIVER_ARRIVING, RIDE_STARTED, IN_RIDE, CANCELLED),
            DRIVER_ACCEPTED, EnumSet.of(DRIVER_ACCEPTED, DRIVER_ARRIVING, RIDE_STARTED, IN_RIDE, CANCELLED),
            DRIVER_ARRIVING, EnumSet.of(RIDE_STARTED, IN_RIDE, CANCELLED),
            RIDE_STARTED, EnumSet.of(IN_RIDE, COMPLETED, CANCELLED),
            IN_RIDE, EnumSet.of(COMPLETED, CANCELLED),
            COMPLETED, EnumSet.noneOf(BookingStatus.class),
            CANCELLED, EnumSet.noneOf(BookingStatus.class)
    );

    public boolean canTransitionTo(BookingStatus target) {
        Set<BookingStatus> validNextStates = ALLOWED_TRANSITIONS.get(this);
        return validNextStates != null && validNextStates.contains(target);
    }
}
