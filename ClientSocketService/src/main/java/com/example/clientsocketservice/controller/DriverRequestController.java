package com.example.clientsocketservice.controller;

import com.example.clientsocketservice.Producers.KafkaProducerService;
import com.example.clientsocketservice.dto.RideRequestDto;
import com.example.clientsocketservice.dto.RideResponseDto;
import com.example.clientsocketservice.dto.UpdateBookingRequestDto;
import com.example.clientsocketservice.dto.UpdateBookingResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RestController
@RequestMapping("/api/socket")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DriverRequestController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RestTemplate restTemplate;
    private final KafkaProducerService kafkaProducerService;

    @Value("${booking.service.url:http://localhost:7777}")
    private String bookingServiceUrl;

    public DriverRequestController(SimpMessagingTemplate simpMessagingTemplate, KafkaProducerService kafkaProducerService1) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.kafkaProducerService = kafkaProducerService1;
        this.restTemplate = new RestTemplate();
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DriverRequestController.class);

    @PostMapping("/newride")
    public ResponseEntity<Boolean> raiseRideRequest(@RequestBody RideRequestDto requestDto) {
        logger.info("Received new ride request for Booking ID: {}, Passenger ID: {}", requestDto.getBookingId(), requestDto.getPassengerId());
        sendDriversNewRideRequest(requestDto);
        return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
    }

    public void sendDriversNewRideRequest(RideRequestDto requestDto) {
        logger.info("Broadcasting ride request for Booking ID: {} to nearby drivers on STOMP topic /topic/rideRequest", requestDto.getBookingId());
        simpMessagingTemplate.convertAndSend("/topic/rideRequest", requestDto);
    }

    @MessageMapping("/rideResponse/{userId}")
    public synchronized void rideResponseHandler(@DestinationVariable String userId, RideResponseDto responseDto) {
        logger.info("Received response from driver: {} for user: {} bookingId: {}", responseDto.getResponse(), userId, responseDto.getBookingId());

        boolean isAccept = Boolean.TRUE.equals(responseDto.getResponse());
        String targetStatus = isAccept ? "DRIVER_ASSIGNED" : "CANCELLED";

        UpdateBookingRequestDto requestDto = isAccept ?
                UpdateBookingRequestDto.builder().driverId(Long.parseLong(userId)).status(targetStatus).build() :
                UpdateBookingRequestDto.builder().status(targetStatus).build();

        // 1. Internally update BookingService database details
        updateBookingServiceDetails(responseDto.getBookingId(), requestDto);

        // 2. Notify STOMP topic /topic/rideResponse
        try {
            simpMessagingTemplate.convertAndSend("/topic/rideResponse", responseDto);
        } catch (Exception e) {
            logger.error("Failed to send rideResponse to STOMP topic", e);
        }

        // 3. Notify Passenger on /topic/passengerNotification
        try {
            java.util.Map<String, Object> passengerNotification = java.util.Map.of(
                    "bookingId", responseDto.getBookingId(),
                    "response", isAccept,
                    "status", targetStatus,
                    "driverId", userId,
                    "message", isAccept ? "Driver #" + userId + " accepted your ride request!" : "Driver declined your ride request."
            );
            simpMessagingTemplate.convertAndSend("/topic/passengerNotification", passengerNotification);
            logger.info("Passenger notified on /topic/passengerNotification for Booking ID: {}, Status: {}", responseDto.getBookingId(), targetStatus);
        } catch (Exception e) {
            logger.error("Failed to notify passenger topic", e);
        }

        try {
            kafkaProducerService.publishMessage("sample-topic", "Driver response recorded for booking " + responseDto.getBookingId());
        } catch (Exception e) {
            logger.warn("Kafka publish skipped: {}", e.getMessage());
        }
    }

    private void updateBookingServiceDetails(Long bookingId, UpdateBookingRequestDto requestDto) {
        String[] possibleUrls = {
                bookingServiceUrl + "/api/v1/booking/" + bookingId,
                "http://booking-service:7777/api/v1/booking/" + bookingId,
                "http://localhost:7777/api/v1/booking/" + bookingId
        };

        for (String url : possibleUrls) {
            try {
                ResponseEntity<UpdateBookingResponseDto> result = this.restTemplate.postForEntity(
                        url,
                        requestDto,
                        UpdateBookingResponseDto.class
                );
                logger.info("Booking {} internally updated via BookingService ({}): status={}", bookingId, url, result.getStatusCode());
                return;
            } catch (Exception e) {
                logger.warn("Attempt to update BookingService at {} failed: {}", url, e.getMessage());
            }
        }
    }

    @GetMapping
    public Boolean help() {
        try {
            kafkaProducerService.publishMessage("sample-topic", "Hello from kafka");
        } catch (Exception e) {
            System.err.println("Kafka publish skipped: " + e.getMessage());
        }
        return Boolean.TRUE;
    }
}
