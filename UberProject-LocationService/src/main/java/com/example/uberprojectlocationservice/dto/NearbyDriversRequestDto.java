package com.example.uberprojectlocationservice.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDriversRequestDto {
    String driverId;
    Double latitude;
    Double longitude;
}
