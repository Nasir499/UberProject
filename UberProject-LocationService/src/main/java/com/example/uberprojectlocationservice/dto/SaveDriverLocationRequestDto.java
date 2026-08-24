package com.example.uberprojectlocationservice.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveDriverLocationRequestDto {
    String driverId;

    Double latitude;

    Double longitude;
}
