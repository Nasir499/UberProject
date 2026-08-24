package com.example.uberbookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent<T> {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    @Builder.Default
    private String eventVersion = "v1";
    private Long rideId;
    @Builder.Default
    private Long timestamp = Instant.now().toEpochMilli();
    private String correlationId;
    @Builder.Default
    private String producer = "UberBookingService";
    private T payload;
}
