package com.example.AirbnbBookingSpring.readModles;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AirbnbReadModel {
    private long id;

    private String name;
    private String description;
    private long  pricePerNight;
    private String location;

    private List<AvailabilityReadModel>availability;
}
