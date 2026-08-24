package com.example.uberprojectauthservice.services;

import com.example.uberentityservice.models.Driver;
import com.example.uberentityservice.models.DriverApprovalStatus;
import com.example.uberentityservice.models.DriverState;
import com.example.uberentityservice.models.Passenger;
import com.example.uberprojectauthservice.dto.DriverDto;
import com.example.uberprojectauthservice.dto.DriverSignupRequestDto;
import com.example.uberprojectauthservice.dto.PassengerDto;
import com.example.uberprojectauthservice.dto.PassengerSignupRequestDto;
import com.example.uberprojectauthservice.repository.DriverRepository;
import com.example.uberprojectauthservice.repository.PassengerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public AuthService(PassengerRepository passengerRepository, DriverRepository driverRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
    }

    public PassengerDto signupPassenger(PassengerSignupRequestDto passengerSignupRequestDto) {
        Passenger passenger = Passenger.builder()
                .email(passengerSignupRequestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(passengerSignupRequestDto.getPassword()))
                .name(passengerSignupRequestDto.getName())
                .phoneNumber(passengerSignupRequestDto.getPhoneNumber())
                .build();
        Passenger newPassenger = passengerRepository.save(passenger);
        return PassengerDto.from(newPassenger);
    }

    public DriverDto signupDriver(DriverSignupRequestDto driverSignupRequestDto) {
        String encodedPassword = driverSignupRequestDto.getPassword() != null ? bCryptPasswordEncoder.encode(driverSignupRequestDto.getPassword()) : null;
        Driver driver = Driver.builder()
                .name(driverSignupRequestDto.getName())
                .email(driverSignupRequestDto.getEmail())
                .password(encodedPassword)
                .phoneNumber(driverSignupRequestDto.getPhoneNumber())
                .licenseNumber(driverSignupRequestDto.getLicenseNumber())
                .aadharCard(driverSignupRequestDto.getAadharCard())
                .driverApprovalStatus(DriverApprovalStatus.APPROVED)
                .driverState(DriverState.AVAILABLE)
                .isAvailable(true)
                .rating(5.0)
                .build();
        Driver savedDriver = driverRepository.save(driver);
        return DriverDto.from(savedDriver);
    }
}
