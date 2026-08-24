package com.example.uberprojectauthservice.dto;

import com.example.uberentityservice.models.Driver;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverDto {
    private Long id;
    private String name;
    private String email;
    private String licenseNumber;
    private String phoneNumber;
    private String aadharCard;
    private Date createdAt;

    public static DriverDto from(Driver driver) {
        return DriverDto.builder()
                .id(driver.getId())
                .name(driver.getName())
                .email(driver.getEmail())
                .licenseNumber(driver.getLicenseNumber())
                .phoneNumber(driver.getPhoneNumber())
                .aadharCard(driver.getAadharCard())
                .createdAt(driver.getCreatedAt())
                .build();
    }
}
