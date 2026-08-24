package com.example.clientsocketservice.dto;

import com.example.uberentityservice.models.Driver;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingResponseDto {
    private Long bookingId;
    private String status;
    private Optional<Driver> driver;
}
