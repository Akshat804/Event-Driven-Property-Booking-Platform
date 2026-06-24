package com.example.AirbnbBookingSpring.Services;

import com.example.AirbnbBookingSpring.DTOs.CreateBookingRequest;
import com.example.AirbnbBookingSpring.DTOs.UpdateBookingRequest;
import com.example.AirbnbBookingSpring.models.Booking;

public interface IBookingService {
    Booking createBooking (CreateBookingRequest createBookingRequest);
    Booking updateBooking (UpdateBookingRequest updateBookingRequest);
   // void deleteBooking (Booking booking);
}