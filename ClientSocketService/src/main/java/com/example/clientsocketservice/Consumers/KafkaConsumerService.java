package com.example.clientsocketservice.Consumers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(SimpMessagingTemplate simpMessagingTemplate, ObjectMapper objectMapper) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = {"ride.requested.v1", "ride.driver_assigned.v1", "ride.driver_accepted.v1", "ride.driver_arriving.v1", "ride.ride_started.v1", "ride.in_ride.v1", "ride.completed.v1", "ride.cancelled.v1", "sample-topic"}, 
        autoStartup = "${spring.kafka.listener.auto-startup:true}"
    )
    public void listen(String message) {
        logger.info("Kafka domain event received: {}", message);

        // Forward to drivers
        try {
            simpMessagingTemplate.convertAndSend("/topic/rideRequest", message);
        } catch (Exception e) {
            logger.error("Failed to forward Kafka event to /topic/rideRequest", e);
        }

        // Forward to passengers - notify them about driver accept/decline/status changes
        try {
            simpMessagingTemplate.convertAndSend("/topic/passengerNotification", message);
            logger.info("Passenger notified via /topic/passengerNotification from Kafka event");
        } catch (Exception e) {
            logger.error("Failed to forward Kafka event to /topic/passengerNotification", e);
        }
    }
}

