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
    private static final String DRIVER_LAST_UPDATE_KEY_PREFIX = "driver_last_update:";
    private static final long UPDATE_INTERVAL_MS = 2 * 60 * 1000; // 2 minutes window
    private static final double LOCATION_DELTA_THRESHOLD = 0.0001; // ~11m delta
    private static final Double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    private final StringRedisTemplate redisTemplate;

    public RedisLocationServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Boolean saveDriverLocation(String driverId, Double latitude, Double longitude) {
        try {
            String trackingKey = DRIVER_LAST_UPDATE_KEY_PREFIX + driverId;
            String lastData = redisTemplate.opsForValue().get(trackingKey);

            long now = System.currentTimeMillis();
            boolean shouldUpdate = true;

            if (lastData != null && !lastData.isBlank()) {
                String[] parts = lastData.split(":");
                if (parts.length == 3) {
                    double lastLat = Double.parseDouble(parts[0]);
                    double lastLon = Double.parseDouble(parts[1]);
                    long lastTime = Long.parseLong(parts[2]);

                    boolean hasLocationChanged = Math.abs(lastLat - latitude) > LOCATION_DELTA_THRESHOLD
                            || Math.abs(lastLon - longitude) > LOCATION_DELTA_THRESHOLD;
                    long timeElapsed = now - lastTime;

                    if (!hasLocationChanged && timeElapsed < UPDATE_INTERVAL_MS) {
                        logger.info("Driver {} location has not changed and update interval has not elapsed yet. Skipping update.", driverId);
                        shouldUpdate = false;
                    }
                }
            }

            if (shouldUpdate) {
                GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
                // Point constructor takes (longitude, latitude)
                geoOps.add(
                        DRIVER_GEO_OPS_KEY,
                        new RedisGeoCommands.GeoLocation<>(
                                driverId,
                                new Point(longitude, latitude)
                        )
                );
                String valToStore = latitude + ":" + longitude + ":" + now;
                redisTemplate.opsForValue().set(trackingKey, valToStore, java.time.Duration.ofMinutes(15));
                logger.info("Saved updated location for driver {}: lat={}, lon={}", driverId, latitude, longitude);
            }
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

            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                    .includeCoordinates()
                    .sortAscending()
                    .limit(50);

            GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(DRIVER_GEO_OPS_KEY, within, args);
            if (results != null) {
                for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                    RedisGeoCommands.GeoLocation<String> location = result.getContent();
                    String driverId = location.getName();
                    Point point = location.getPoint();
                    if (point != null) {
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
