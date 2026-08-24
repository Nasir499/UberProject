package com.example.clientsocketservice.models;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExactLocation {
    private Double latitude;
    private Double longitude;
}
