package com.example.AirbnbBookingSpring.Services;

import com.example.AirbnbBookingSpring.models.Booking;

import java.util.Optional;

public interface IidempotencyService {
    boolean isIdempotencyKeyUsed(String idempotencyKey);
    Optional<Booking> findBookingByIdempotencyKey(String idempotencyKey);

}
