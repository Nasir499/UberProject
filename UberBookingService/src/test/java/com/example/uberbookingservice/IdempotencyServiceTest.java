package com.example.uberbookingservice;

import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberbookingservice.services.IdempotencyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class IdempotencyServiceTest {

    @Test
    @DisplayName("Verify idempotency key stores and returns cached response for duplicate request")
    public void testIdempotencyCaching() {
        IdempotencyService idempotencyService = new IdempotencyService();
        String key = "test-idempotency-key-12345";

        CreateBookingResponseDto originalResponse = CreateBookingResponseDto.builder()
                .bookingId("101")
                .bookingStatus("REQUESTED")
                .build();

        // Initially key should not exist
        Assertions.assertTrue(idempotencyService.getExistingResponse(key).isEmpty());

        // Store response
        idempotencyService.storeResponse(key, originalResponse);

        // Subsequent lookup with same key must return cached response
        Optional<CreateBookingResponseDto> cachedResponse = idempotencyService.getExistingResponse(key);
        Assertions.assertTrue(cachedResponse.isPresent());
        Assertions.assertEquals("101", cachedResponse.get().getBookingId());
        Assertions.assertEquals("REQUESTED", cachedResponse.get().getBookingStatus());
    }

    @Test
    @DisplayName("Verify null or blank idempotency key returns empty optional")
    public void testNullIdempotencyKey() {
        IdempotencyService idempotencyService = new IdempotencyService();
        Assertions.assertTrue(idempotencyService.getExistingResponse(null).isEmpty());
        Assertions.assertTrue(idempotencyService.getExistingResponse("   ").isEmpty());
    }
}
