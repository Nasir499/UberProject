package com.example.uberbookingservice.services;

import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);
    private static final String REDIS_KEY_PREFIX = "idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, CreateBookingResponseDto> inMemoryFallbackStore = new ConcurrentHashMap<>();

    public IdempotencyService() {
        this(null, new ObjectMapper());
    }

    @Autowired
    public IdempotencyService(@Autowired(required = false) StringRedisTemplate redisTemplate, @Autowired(required = false) ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public Optional<CreateBookingResponseDto> getExistingResponse(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }

        String redisKey = REDIS_KEY_PREFIX + key;

        if (redisTemplate != null) {
            try {
                String jsonVal = redisTemplate.opsForValue().get(redisKey);
                if (jsonVal != null && !jsonVal.isBlank()) {
                    CreateBookingResponseDto response = objectMapper.readValue(jsonVal, CreateBookingResponseDto.class);
                    logger.info("Found idempotency key {} in Redis cache", key);
                    return Optional.of(response);
                }
            } catch (Exception e) {
                logger.warn("Redis error on getExistingResponse for key {}: {}. Falling back to in-memory store.", key, e.getMessage());
            }
        }

        return Optional.ofNullable(inMemoryFallbackStore.get(key));
    }

    public void storeResponse(String key, CreateBookingResponseDto response) {
        if (key == null || key.isBlank() || response == null) {
            return;
        }

        String redisKey = REDIS_KEY_PREFIX + key;

        if (redisTemplate != null) {
            try {
                String jsonVal = objectMapper.writeValueAsString(response);
                redisTemplate.opsForValue().set(redisKey, jsonVal, DEFAULT_TTL);
                logger.info("Stored idempotency key {} in Redis cache with TTL {} minutes", key, DEFAULT_TTL.toMinutes());
                return;
            } catch (Exception e) {
                logger.warn("Redis error on storeResponse for key {}: {}. Falling back to in-memory store.", key, e.getMessage());
            }
        }

        inMemoryFallbackStore.put(key, response);
    }
}
