package com.example.uberbookingservice.dto;

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
