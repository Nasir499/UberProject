package com.example.uberprojectauthservice.controllers;

import com.example.uberprojectauthservice.dto.*;
import com.example.uberprojectauthservice.services.AuthService;
import com.example.uberprojectauthservice.services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Value("${cookie.expiry:86400}")
    private int cookieExpiry;

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new passenger.
     */
    @PostMapping("/signup/passenger")
    public ResponseEntity<PassengerDto> signUpPassenger(@RequestBody PassengerSignupRequestDto passengerSignupRequestdto) {
        PassengerDto response = authService.signupPassenger(passengerSignupRequestdto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Registers a new driver.
     */
    @PostMapping("/signup/driver")
    public ResponseEntity<DriverDto> signUpDriver(@RequestBody DriverSignupRequestDto driverSignupRequestDto) {
        DriverDto response = authService.signupDriver(driverSignupRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Signs in a passenger.
     */
    @PostMapping("/signin/passenger")
    public ResponseEntity<?> signInPassenger(@RequestBody AuthRequestDto authRequestDto, HttpServletResponse response) {
        return authenticateAndGenerateCookie(authRequestDto, response);
    }

    /**
     * Signs in a driver.
     */
    @PostMapping("/signin/driver")
    public ResponseEntity<?> signInDriver(@RequestBody AuthRequestDto authRequestDto, HttpServletResponse response) {
        return authenticateAndGenerateCookie(authRequestDto, response);
    }

    private ResponseEntity<?> authenticateAndGenerateCookie(AuthRequestDto authRequestDto, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword())
        );
        if (authentication.isAuthenticated()) {
            String jwtToken = jwtService.createToken(authRequestDto.getEmail());
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(cookieExpiry)
                    .build();
            response.setHeader("set-cookie", cookie.toString());
            return new ResponseEntity<>(AuthResponseDto.builder().success(true).build(), HttpStatus.OK);
        } else {
            throw new UsernameNotFoundException("Invalid Credentials");
        }
    }
}
