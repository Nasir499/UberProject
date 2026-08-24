package com.example.uberentityservice.models;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum DriverState {
    OFFLINE,
    AVAILABLE,
    RESERVED,
    ON_TRIP;

    private static final Map<DriverState, Set<DriverState>> ALLOWED_TRANSITIONS = Map.of(
            OFFLINE, EnumSet.of(AVAILABLE),
            AVAILABLE, EnumSet.of(RESERVED, OFFLINE),
            RESERVED, EnumSet.of(ON_TRIP, AVAILABLE, OFFLINE),
            ON_TRIP, EnumSet.of(AVAILABLE, OFFLINE)
    );

    public boolean canTransitionTo(DriverState target) {
        Set<DriverState> validNextStates = ALLOWED_TRANSITIONS.get(this);
        return validNextStates != null && validNextStates.contains(target);
    }
}
