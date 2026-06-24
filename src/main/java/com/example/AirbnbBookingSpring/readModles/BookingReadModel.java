package com.example.AirbnbBookingSpring.readModles;

import com.example.AirbnbBookingSpring.models.Booking;
import jakarta.persistence.Column;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BookingReadModel {
    private long id;
    private long userId;
    private long airbnbId;

    private long totalPrice;
    private String bookingStatus;
    private String idempotency;
    private String CheckOutdate;
    private String CheckIndate;


}
