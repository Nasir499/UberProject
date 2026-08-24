package com.example.uberbookingservice.dto;

import com.example.uberentityservice.models.Driver;
import lombok.*;

import java.util.Optional;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingResponseDto {
    private String bookingId;

    private String bookingStatus;

    private Optional<Driver> driver;


}
