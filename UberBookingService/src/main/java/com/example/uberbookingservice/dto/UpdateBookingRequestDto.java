package com.example.uberbookingservice.dto;

import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequestDto {
    private String status;
    private Long driverId;

    public Optional<Long> getDriverId() {
        return Optional.ofNullable(driverId);
    }
}
