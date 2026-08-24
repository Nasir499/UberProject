package com.example.uberprojectlocationservice.controllers;

import com.example.uberprojectlocationservice.dto.DriverLocationDto;
import com.example.uberprojectlocationservice.dto.NearbyDriversRequestDto;
import com.example.uberprojectlocationservice.dto.SaveDriverLocationRequestDto;
import com.example.uberprojectlocationservice.services.LocationService;
import org.springframework.data.geo.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    LocationService locationService;

   public LocationController(LocationService locationService) {
       this.locationService = locationService;
   }

    @PostMapping({"/setDriverLocation", "/drivers"})
    public ResponseEntity<Boolean> saveDriverLocation(@RequestBody SaveDriverLocationRequestDto saveDriverLocationRequestDto) {
        try{
           Boolean response = locationService.saveDriverLocation(saveDriverLocationRequestDto.getDriverId(),saveDriverLocationRequestDto.getLatitude(),saveDriverLocationRequestDto.getLongitude());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping ("/nearby/drivers")
    public ResponseEntity<List<DriverLocationDto>> getNearByDrivers(@RequestBody NearbyDriversRequestDto nearbyDriversRequestDto){
       try {
          List<DriverLocationDto> drivers = locationService.getNearbyDrivers(nearbyDriversRequestDto.getLatitude(),nearbyDriversRequestDto.getLongitude());
           return new ResponseEntity<>(drivers,HttpStatus.OK);
       }catch (Exception e){
           return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
         }
       }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<DriverLocationDto> getDriverLocation(@PathVariable("driverId") String driverId) {
        try {
            DriverLocationDto driverLocation = locationService.getDriverLocation(driverId);
            if (driverLocation != null) {
                return new ResponseEntity<>(driverLocation, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
