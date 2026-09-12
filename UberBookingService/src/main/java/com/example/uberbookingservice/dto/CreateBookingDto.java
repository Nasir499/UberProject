package com.example.uberbookingservice.dto;

import com.example.uberentityservice.models.ExactLocation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDto {

    @NotNull(message = "Passenger ID is required")
    private Long passengerId;

    private ExactLocation startLocation;

    private ExactLocation endLocation;
}
