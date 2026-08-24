package com.example.uberbookingservice;

import com.example.uberbookingservice.controllers.BookingController;
import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberentityservice.models.ExactLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ApiIntegrationTest {

    @Autowired
    private BookingController bookingController;

    @Test
    @DisplayName("Test Create Booking API with Idempotency Key")
    public void testCreateBookingApi() {
        ExactLocation start = ExactLocation.builder().latitude(37.7749).longitude(-122.4194).build();
        ExactLocation end = ExactLocation.builder().latitude(37.7833).longitude(-122.4167).build();

        CreateBookingDto request = CreateBookingDto.builder()
                .passengerId(1L)
                .startLocation(start)
                .endLocation(end)
                .build();

        String idempotencyKey = "api-test-key-77777";

        // Call API endpoint
        ResponseEntity<CreateBookingResponseDto> response = bookingController.createBooking(request, idempotencyKey);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(response.getBody().getBookingId());
        Assertions.assertEquals("REQUESTED", response.getBody().getBookingStatus());

        // Repeat API call with SAME Idempotency-Key
        ResponseEntity<CreateBookingResponseDto> duplicateResponse = bookingController.createBooking(request, idempotencyKey);

        Assertions.assertEquals(HttpStatus.CREATED, duplicateResponse.getStatusCode());
        Assertions.assertEquals(response.getBody().getBookingId(), duplicateResponse.getBody().getBookingId(), "Idempotent request must return identical bookingId");
    }
}
