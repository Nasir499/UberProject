package com.example.uberbookingservice.controllers;

import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberbookingservice.dto.UpdateBookingResponseDto;
import com.example.uberbookingservice.dto.UpdateBookingRequestDto;
import com.example.uberbookingservice.services.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {
    private final BookingService bookingService;
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<CreateBookingResponseDto> createBooking(
            @Valid @RequestBody CreateBookingDto createBookingDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return new ResponseEntity<>(bookingService.createBooking(createBookingDto, idempotencyKey), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{bookingId}", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<UpdateBookingResponseDto> updateBooking(@Valid @RequestBody UpdateBookingRequestDto updateBookingDto, @PathVariable Long bookingId) {
        return new ResponseEntity<>(bookingService.updateBooking(updateBookingDto, bookingId), HttpStatus.OK);
    }
}

