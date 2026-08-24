package com.example.uberprojectauthservice.services;

import com.example.uberentityservice.models.Driver;
import com.example.uberentityservice.models.Passenger;
import com.example.uberprojectauthservice.helpers.AuthDriverDetails;
import com.example.uberprojectauthservice.helpers.AuthPassengerDetails;
import com.example.uberprojectauthservice.repository.DriverRepository;
import com.example.uberprojectauthservice.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Passenger> passenger = passengerRepository.findPassengerByEmail(email);
        if (passenger.isPresent()) {
            return new AuthPassengerDetails(passenger.get());
        }
        
        Optional<Driver> driver = driverRepository.findDriverByEmail(email);
        if (driver.isPresent()) {
            return new AuthDriverDetails(driver.get());
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
