package com.example.AirbnbBookingSpring.DTOs;

import com.example.AirbnbBookingSpring.models.Booking;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateBookingRequest {

    @NotNull(message = "Booking Id is Required")
    private long id;
    @NotNull(message = "Idempotency Key is Required")
    private String IdempotencyKey;
    @NotNull(message="Booking Status Is Required")
    private Booking.BookingStatus bookingstatus;

}
