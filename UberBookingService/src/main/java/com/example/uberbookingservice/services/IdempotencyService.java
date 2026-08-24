package com.example.uberbookingservice.services;

import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    private final Map<String, CreateBookingResponseDto> idempotencyStore = new ConcurrentHashMap<>();

    public Optional<CreateBookingResponseDto> getExistingResponse(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(idempotencyStore.get(key));
    }

    public void storeResponse(String key, CreateBookingResponseDto response) {
        if (key != null && !key.isBlank() && response != null) {
            idempotencyStore.put(key, response);
        }
    }
}
