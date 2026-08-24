package com.example.uberbookingservice.services;

import com.example.uberbookingservice.dto.CreateBookingDto;
import com.example.uberbookingservice.dto.CreateBookingResponseDto;
import com.example.uberbookingservice.dto.UpdateBookingRequestDto;
import com.example.uberbookingservice.dto.UpdateBookingResponseDto;
import com.example.uberentityservice.models.Booking;

public interface BookingService {
    CreateBookingResponseDto createBooking(CreateBookingDto createBooking);
    CreateBookingResponseDto createBooking(CreateBookingDto createBooking, String idempotencyKey);
    UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingRequestDto, Long bookingId);
    UpdateBookingResponseDto getBookingStatus(Long bookingId);
}

