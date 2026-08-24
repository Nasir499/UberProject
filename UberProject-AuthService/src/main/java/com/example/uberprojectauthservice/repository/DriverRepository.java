package com.example.uberprojectauthservice.repository;

import com.example.uberentityservice.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findDriverByEmail(String email);
    Optional<Driver> findDriverByLicenseNumber(String licenseNumber);
    Optional<Driver> findDriverByPhoneNumber(String phoneNumber);
}
