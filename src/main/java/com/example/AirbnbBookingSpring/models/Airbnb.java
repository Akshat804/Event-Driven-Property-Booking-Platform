package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Airbnb {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
   @Column(nullable = false)
    private String name;
   @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private double  pricePerNight;
    @Column(nullable = false)
    private String location;

}
