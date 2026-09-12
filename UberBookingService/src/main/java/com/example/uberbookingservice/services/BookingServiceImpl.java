package com.example.uberbookingservice.services;

import com.example.uberbookingservice.apis.LocationServiceApi;
import com.example.uberbookingservice.apis.UberSocketApi;
import com.example.uberbookingservice.dto.*;
import com.example.uberbookingservice.repositories.BookingRepository;
import com.example.uberbookingservice.repositories.DriverRepository;
import com.example.uberbookingservice.repositories.PassengerRepository;
import com.example.uberentityservice.models.Booking;
import com.example.uberentityservice.models.BookingStatus;
import com.example.uberentityservice.models.Driver;
import com.example.uberentityservice.models.Passenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final UberSocketApi uberSocketApi;
    private final LocationServiceApi locationServiceApi;
    private final DriverRepository driverRepository;
    private final IdempotencyService idempotencyService;
    private final KafkaEventProducerService kafkaEventProducerService;

    public BookingServiceImpl(PassengerRepository passengerRepository,
                              BookingRepository bookingRepository,
                              UberSocketApi uberSocketApi,
                              LocationServiceApi locationServiceApi,
                              DriverRepository driverRepository,
                              IdempotencyService idempotencyService,
                              KafkaEventProducerService kafkaEventProducerService) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.uberSocketApi = uberSocketApi;
        this.locationServiceApi = locationServiceApi;
        this.driverRepository = driverRepository;
        this.idempotencyService = idempotencyService;
        this.kafkaEventProducerService = kafkaEventProducerService;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails) {
        return createBooking(bookingDetails, null);
    }

    @Override
    @Transactional
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<CreateBookingResponseDto> cached = idempotencyService.getExistingResponse(idempotencyKey);
            if (cached.isPresent()) {
                logger.info("Idempotency key hit for key: {}, returning cached booking response", idempotencyKey);
                return cached.get();
            }
        }

        Optional<Passenger> passenger = passengerRepository.findById(bookingDetails.getPassengerId());
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.REQUESTED)
                .startLocation(bookingDetails.getStartLocation())
                .endLocation(bookingDetails.getEndLocation())
                .passenger(passenger.orElse(null))
                .build();
        Booking newBooking = bookingRepository.save(booking);

        CreateBookingResponseDto responseDto = CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId().toString())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .build();

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.storeResponse(idempotencyKey, responseDto);
        }

        // Publish domain event
        DomainEvent<CreateBookingResponseDto> event = DomainEvent.<CreateBookingResponseDto>builder()
                .eventType("ride.requested.v1")
                .rideId(newBooking.getId())
                .payload(responseDto)
                .build();

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaEventProducerService.sendDomainEvent("ride.requested.v1", event);
                }
            });
        } else {
            kafkaEventProducerService.sendDomainEvent("ride.requested.v1", event);
        }

        Double lat = (bookingDetails.getStartLocation() != null && bookingDetails.getStartLocation().getLatitude() != null)
                ? bookingDetails.getStartLocation().getLatitude() : 28.6139;
        Double lng = (bookingDetails.getStartLocation() != null && bookingDetails.getStartLocation().getLongitude() != null)
                ? bookingDetails.getStartLocation().getLongitude() : 77.2090;

        NearbyDriversRequestDto request = NearbyDriversRequestDto.builder()
                .latitude(lat)
                .longitude(lng)
                .build();

        processNearbyDriversAsync(request, bookingDetails.getPassengerId(), newBooking.getId());

        return responseDto;
    }

    @Override
    public UpdateBookingResponseDto getBookingStatus(Long bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new IllegalArgumentException("Booking not found with ID: " + bookingId);
        }
        Booking booking = optionalBooking.get();
        return UpdateBookingResponseDto.builder()
                .bookingId(bookingId)
                .status(booking.getBookingStatus().name())
                .driver(Optional.ofNullable(booking.getDriver()))
                .build();
    }

    @Override
    @Transactional
    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingRequestDto, Long bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new IllegalArgumentException("Booking not found with ID: " + bookingId);
        }

        Booking booking = optionalBooking.get();
        BookingStatus currentStatus = booking.getBookingStatus();
        BookingStatus targetStatus = BookingStatus.valueOf(bookingRequestDto.getStatus());

        if (currentStatus != null && !currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException(String.format("Invalid state transition from %s to %s for booking %d",
                    currentStatus, targetStatus, bookingId));
        }

        // Prevent double driver assignment if booking is already accepted by another driver
        if (targetStatus == BookingStatus.DRIVER_ACCEPTED && booking.getBookingStatus() == BookingStatus.DRIVER_ACCEPTED) {
            if (booking.getDriver() != null && bookingRequestDto.getDriverId().isPresent()
                    && !booking.getDriver().getId().equals(bookingRequestDto.getDriverId().get())) {
                throw new IllegalStateException(String.format("Booking %d has already been accepted by driver %d",
                        bookingId, booking.getDriver().getId()));
            }
        }

        Optional<Driver> driverOpt = bookingRequestDto.getDriverId().flatMap(driverRepository::findById);
        if (bookingRequestDto.getDriverId().isPresent() && driverOpt.isEmpty()) {
            throw new IllegalArgumentException("Driver not found with ID: " + bookingRequestDto.getDriverId().get());
        }

        if (targetStatus == BookingStatus.DRIVER_ASSIGNED || targetStatus == BookingStatus.DRIVER_ACCEPTED) {
            if (driverOpt.isEmpty()) {
                throw new IllegalArgumentException("Driver ID is required for state " + targetStatus);
            }
            Long driverId = driverOpt.get().getId();
            int reservedRows = driverRepository.reserveDriverIfAvailable(driverId);
            if (reservedRows == 0 && (booking.getDriver() == null || !booking.getDriver().getId().equals(driverId))) {
                logger.warn("Driver {} was not available for reservation on booking {}", driverId, bookingId);
            }
        }

        Driver driver = driverOpt.orElse(booking.getDriver());
        booking.setBookingStatus(targetStatus);
        booking.setDriver(driver);
        bookingRepository.save(booking);

        UpdateBookingResponseDto responseDto = UpdateBookingResponseDto.builder()
                .bookingId(bookingId)
                .status(targetStatus.name())
                .driver(Optional.ofNullable(driver))
                .build();

        // Publish state change domain event
        String eventTopic = "ride." + targetStatus.name().toLowerCase() + ".v1";
        DomainEvent<UpdateBookingResponseDto> event = DomainEvent.<UpdateBookingResponseDto>builder()
                .eventType(eventTopic)
                .rideId(bookingId)
                .payload(responseDto)
                .build();

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaEventProducerService.sendDomainEvent(eventTopic, event);
                }
            });
        } else {
            kafkaEventProducerService.sendDomainEvent(eventTopic, event);
        }

        return responseDto;
    }

    private void processNearbyDriversAsync(NearbyDriversRequestDto request, Long passengerId, Long bookingId) {
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(request);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                DriverLocationDto[] nearbyDrivers = response.body();
                Long assignedDriverId = tryAutoAssignDriver(nearbyDrivers, bookingId);
                logger.info("Auto-assigned driver {} for booking {}. Raising ride request to socket service.", assignedDriverId, bookingId);
                
                List<Long> driverIds = new java.util.ArrayList<>();
                if (nearbyDrivers != null && nearbyDrivers.length > 0) {
                    for (DriverLocationDto d : nearbyDrivers) {
                        try {
                            if (d.getDriverId() != null) {
                                driverIds.add(Long.parseLong(d.getDriverId()));
                            }
                        } catch (Exception ignored) {}
                    }
                }
                if (assignedDriverId != null && !driverIds.contains(assignedDriverId)) {
                    driverIds.add(assignedDriverId);
                }

                Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
                com.example.uberentityservice.models.ExactLocation startLoc = bookingOpt.map(Booking::getStartLocation).orElse(null);
                com.example.uberentityservice.models.ExactLocation endLoc = bookingOpt.map(Booking::getEndLocation).orElse(null);

                logger.info("Notifying nearby drivers {} for booking {}", driverIds, bookingId);
                raiseRideRequestAsync(RideRequestDto.builder()
                        .passengerId(passengerId)
                        .bookingId(bookingId)
                        .driverIds(driverIds)
                        .startLocation(startLoc)
                        .endLocation(endLoc)
                        .build());
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable t) {
                logger.warn("Failed to fetch nearby drivers for booking {}, attempting fallback driver assignment", bookingId, t);
                Long assignedDriverId = tryAutoAssignDriver(null, bookingId);
                List<Long> driverIds = assignedDriverId != null ? List.of(assignedDriverId) : List.of();
                
                Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
                com.example.uberentityservice.models.ExactLocation startLoc = bookingOpt.map(Booking::getStartLocation).orElse(null);
                com.example.uberentityservice.models.ExactLocation endLoc = bookingOpt.map(Booking::getEndLocation).orElse(null);

                raiseRideRequestAsync(RideRequestDto.builder()
                        .passengerId(passengerId)
                        .bookingId(bookingId)
                        .driverIds(driverIds)
                        .startLocation(startLoc)
                        .endLocation(endLoc)
                        .build());
            }
        });
    }

    private Long tryAutoAssignDriver(DriverLocationDto[] nearbyDrivers, Long bookingId) {
        try {
            Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
            if (optionalBooking.isEmpty()) return null;
            Booking booking = optionalBooking.get();

            // Try assigning driver from nearby locations list
            if (nearbyDrivers != null) {
                for (DriverLocationDto dto : nearbyDrivers) {
                    try {
                        Long dId = Long.parseLong(dto.getDriverId());
                        Optional<Driver> driverOpt = driverRepository.findById(dId);
                        if (driverOpt.isPresent()) {
                            int reservedRows = driverRepository.reserveDriverIfAvailable(dId);
                            if (reservedRows > 0) {
                                booking.setDriver(driverOpt.get());
                                booking.setBookingStatus(BookingStatus.DRIVER_ASSIGNED);
                                bookingRepository.save(booking);
                                return dId;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            // Fallback: reserve any available driver from top candidates in repository
            List<Driver> availableDrivers = driverRepository.findTop10ByIsAvailableTrue();
            for (Driver d : availableDrivers) {
                if (d.getIsAvailable() == null || d.getIsAvailable()) {
                    Long dId = d.getId();
                    int reservedRows = driverRepository.reserveDriverIfAvailable(dId);
                    if (reservedRows > 0) {
                        booking.setDriver(d);
                        booking.setBookingStatus(BookingStatus.DRIVER_ASSIGNED);
                        bookingRepository.save(booking);
                        return dId;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error auto-assigning driver for booking {}", bookingId, e);
        }
        return null;
    }

    private void raiseRideRequestAsync(RideRequestDto request) {
        Call<Boolean> call = uberSocketApi.raiseRideRequest(request);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logger.info("Ride request raised successfully: {}", response.body());
                } else {
                    logger.warn("Retrofit raiseRideRequest non-successful ({}), attempting direct fallback", response.code());
                    sendDirectRideRequestToSocket(request);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                logger.warn("Failed to raise ride request via Retrofit, attempting direct fallback", t);
                sendDirectRideRequestToSocket(request);
            }
        });
    }

    private void sendDirectRideRequestToSocket(RideRequestDto request) {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.postForObject("http://socket-service:8086/api/socket/newride", request, Boolean.class);
            logger.info("Successfully sent ride request to socket service via http://socket-service:8086");
            return;
        } catch (Exception ignored) {}

        try {
            restTemplate.postForObject("http://localhost:8086/api/socket/newride", request, Boolean.class);
            logger.info("Successfully sent ride request to socket service via http://localhost:8086");
        } catch (Exception e) {
            logger.error("Failed direct HTTP call to socket service", e);
        }
    }
}

