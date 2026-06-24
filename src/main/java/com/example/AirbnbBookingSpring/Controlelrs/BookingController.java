package com.example.AirbnbBookingSpring.Controlelrs;

import com.example.AirbnbBookingSpring.DTOs.CreateBookingRequest;
import com.example.AirbnbBookingSpring.DTOs.UpdateBookingRequest;
import com.example.AirbnbBookingSpring.Services.BookingService;
import com.example.AirbnbBookingSpring.models.Booking;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody CreateBookingRequest createBookingRequest) {
        return ResponseEntity.ok(bookingService.createBooking(createBookingRequest));
    }

    @PutMapping
    public ResponseEntity<Booking> updateBooking(@RequestBody UpdateBookingRequest updateBookingRequest) {
        return ResponseEntity.ok(bookingService.updateBooking(updateBookingRequest));
    }

}
