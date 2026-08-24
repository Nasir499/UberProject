package com.example.uberbookingservice.services;

import com.example.uberbookingservice.dto.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> void sendDomainEvent(String topic, DomainEvent<T> event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            String key = event.getRideId() != null ? event.getRideId().toString() : event.getEventId();
            logger.info("Publishing domain event to topic {}: {}", topic, event.getEventType());
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(topic, key, jsonPayload);
                } catch (Exception e) {
                    logger.error("Async Kafka send failed: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Failed to publish domain event {} to topic {}", event.getEventType(), topic, e);
        }
    }
}
