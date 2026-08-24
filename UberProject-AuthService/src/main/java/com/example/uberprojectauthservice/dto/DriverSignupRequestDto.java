package com.example.uberprojectauthservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverSignupRequestDto {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String licenseNumber;
    private String aadharCard;
}
