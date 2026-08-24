package com.example.uberbookingservice.repositories;

import com.example.uberentityservice.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Driver d SET d.driverState = com.example.uberentityservice.models.DriverState.RESERVED, d.isAvailable = false WHERE d.id = :driverId AND (d.isAvailable = true OR d.isAvailable IS NULL) AND (d.driverState = com.example.uberentityservice.models.DriverState.AVAILABLE OR d.driverState IS NULL)")
    int reserveDriverIfAvailable(@Param("driverId") Long driverId);
}
