package com.example.uberprojectlocationservice.services;

import com.example.uberprojectlocationservice.dto.DriverLocationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedisLocationServiceImpl implements LocationService {

    private static final Logger logger = LoggerFactory.getLogger(RedisLocationServiceImpl.class);
    private static final String DRIVER_GEO_OPS_KEY = "driver_locations";
    private static final Double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    private final StringRedisTemplate redisTemplate;

    public RedisLocationServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Boolean saveDriverLocation(String driverId, Double latitude, Double longitude) {
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
            // Point constructor takes (longitude, latitude)
            geoOps.add(
                    DRIVER_GEO_OPS_KEY,
                    new RedisGeoCommands.GeoLocation<>(
                            driverId,
                            new Point(longitude, latitude)
                    )
            );
            logger.info("Saved location for driver {}: lat={}, lon={}", driverId, latitude, longitude);
            return true;
        } catch (Exception e) {
            logger.error("Failed to save location for driver {} in Redis", driverId, e);
            return false;
        }
    }

    @Override
    public List<DriverLocationDto> getNearbyDrivers(Double latitude, Double longitude) {
        List<DriverLocationDto> drivers = new ArrayList<>();
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
            Distance distance = new Distance(DEFAULT_SEARCH_RADIUS_KM, Metrics.KILOMETERS);
            // Circle constructor takes Point(longitude, latitude) and Distance
            Circle within = new Circle(new Point(longitude, latitude), distance);

            GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(DRIVER_GEO_OPS_KEY, within);
            if (results != null) {
                for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                    String driverId = result.getContent().getName();
                    List<Point> positions = geoOps.position(DRIVER_GEO_OPS_KEY, driverId);
                    if (positions != null && !positions.isEmpty() && positions.get(0) != null) {
                        Point point = positions.get(0);
                        drivers.add(DriverLocationDto.builder()
                                .driverId(driverId)
                                .latitude(point.getY())  // Y is Latitude
                                .longitude(point.getX()) // X is Longitude
                                .build());
                    }
                }
            }
            logger.info("Found {} drivers within {}km of ({}, {})", drivers.size(), DEFAULT_SEARCH_RADIUS_KM, latitude, longitude);
        } catch (Exception e) {
            logger.error("Failed to query nearby drivers from Redis", e);
        }
        return drivers;
    }

    @Override
    public DriverLocationDto getDriverLocation(String driverId) {
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
            List<Point> positions = geoOps.position(DRIVER_GEO_OPS_KEY, driverId);
            if (positions != null && !positions.isEmpty() && positions.get(0) != null) {
                Point point = positions.get(0);
                return DriverLocationDto.builder()
                        .driverId(driverId)
                        .latitude(point.getY())  // Y is Latitude
                        .longitude(point.getX()) // X is Longitude
                        .build();
            }
        } catch (Exception e) {
            logger.error("Failed to query location for driver {} from Redis", driverId, e);
        }
        return null;
    }
}
