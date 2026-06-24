package com.example.AirbnbBookingSpring.DTOs;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookingRequest {
    @NotNull(message = "AirBnb Id is Required")
    private String airbnbId;
    @NotNull(message =" check-in-date is required")
    private LocalDate checkInDate;
    @NotNull(message="Checkout-date is required")
    private LocalDate checkOutDate;
    @NotNull(message="User Id is required")
    private String userId;

}
