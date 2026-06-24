package com.example.AirbnbBookingSpring.Services;

import com.example.AirbnbBookingSpring.models.Availability;

import java.time.LocalDate;
import java.util.List;

public interface ConcurrencyControlStrategy {
    void releaselock(long airbnbId, LocalDate checkInDate,LocalDate checkOutDate);
    List<Availability> lockAndCheckAvailability(long airbnbId, LocalDate checkInDate, LocalDate checkOutDate,Long userId);
}
