package com.example.uberbookingservice;

import com.example.uberentityservice.models.BookingStatus;
import com.example.uberentityservice.models.DriverState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BookingStatusTest {

    @Test
    @DisplayName("Verify valid ride state machine transitions")
    public void testValidStateTransitions() {
        Assertions.assertTrue(BookingStatus.REQUESTED.canTransitionTo(BookingStatus.DRIVER_ASSIGNED));
        Assertions.assertTrue(BookingStatus.DRIVER_ASSIGNED.canTransitionTo(BookingStatus.DRIVER_ACCEPTED));
        Assertions.assertTrue(BookingStatus.DRIVER_ACCEPTED.canTransitionTo(BookingStatus.DRIVER_ARRIVING));
        Assertions.assertTrue(BookingStatus.DRIVER_ARRIVING.canTransitionTo(BookingStatus.RIDE_STARTED));
        Assertions.assertTrue(BookingStatus.RIDE_STARTED.canTransitionTo(BookingStatus.COMPLETED));

        // Cancellation transitions
        Assertions.assertTrue(BookingStatus.REQUESTED.canTransitionTo(BookingStatus.CANCELLED));
        Assertions.assertTrue(BookingStatus.DRIVER_ASSIGNED.canTransitionTo(BookingStatus.CANCELLED));
        Assertions.assertTrue(BookingStatus.RIDE_STARTED.canTransitionTo(BookingStatus.CANCELLED));
    }

    @Test
    @DisplayName("Verify illegal ride state machine transitions are rejected")
    public void testInvalidStateTransitions() {
        Assertions.assertFalse(BookingStatus.COMPLETED.canTransitionTo(BookingStatus.REQUESTED));
        Assertions.assertFalse(BookingStatus.CANCELLED.canTransitionTo(BookingStatus.RIDE_STARTED));
        Assertions.assertFalse(BookingStatus.REQUESTED.canTransitionTo(BookingStatus.COMPLETED));
        Assertions.assertFalse(BookingStatus.REQUESTED.canTransitionTo(BookingStatus.RIDE_STARTED));
    }

    @Test
    @DisplayName("Verify driver state transitions")
    public void testDriverStateTransitions() {
        Assertions.assertTrue(DriverState.OFFLINE.canTransitionTo(DriverState.AVAILABLE));
        Assertions.assertTrue(DriverState.AVAILABLE.canTransitionTo(DriverState.RESERVED));
        Assertions.assertTrue(DriverState.RESERVED.canTransitionTo(DriverState.ON_TRIP));
        Assertions.assertTrue(DriverState.ON_TRIP.canTransitionTo(DriverState.AVAILABLE));

        // Invalid driver state transition
        Assertions.assertFalse(DriverState.OFFLINE.canTransitionTo(DriverState.ON_TRIP));
    }
}
