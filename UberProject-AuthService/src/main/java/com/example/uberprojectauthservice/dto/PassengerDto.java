package com.example.uberprojectauthservice.dto;

import com.example.uberentityservice.models.Passenger;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PassengerDto {
    private Long id;

    private  String name;

    private  String email;

    private  String password;

    private  String phoneNumber;

    private Date createdAt;

    /**
     * Converts a Passenger entity to a PassengerDto.
     *
     * @param passenger the passenger entity
     * @return the corresponding PassengerDto
     */
    public static PassengerDto from(Passenger passenger) {
        return PassengerDto.builder()
                .id(passenger.getId())
                .createdAt(passenger.getCreatedAt())
                .email(passenger.getEmail())
                .name(passenger.getName())
                .phoneNumber(passenger.getPhoneNumber())
                .password(passenger.getPassword())
                .build();
    }
}
