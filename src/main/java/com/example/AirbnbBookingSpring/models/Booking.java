package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long  id;
    private String userId;
    private long airbnbId;

    private double totalPrice;
    @Column(nullable = false)
    private BookingStatus bookingStatus;
    @Column(unique = true)
    private String Idempotency;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    public enum BookingStatus {
        PENDING,CONFIRMED,CANCELLED
    }


}
