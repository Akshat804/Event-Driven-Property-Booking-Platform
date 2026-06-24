package com.example.AirbnbBookingSpring.readModles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityReadModel {
  private long id;
  private long airbnbId;
  private String date;
  private long bookingId;
  private Boolean IsAvailable;


}
