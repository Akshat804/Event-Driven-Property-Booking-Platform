package com.example.AirbnbBookingSpring.Saga;

import com.example.AirbnbBookingSpring.handlers.AvailabilityHandeler;
import com.example.AirbnbBookingSpring.handlers.BookingEventHandeler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventProcessor {
    private final BookingEventHandeler bookingeventhandeler;
    private final AvailabilityHandeler availabilityhandeler;
    public void processEvent(SagaEvent sagaEvent){
        switch (sagaEvent.getEventType()) {
            case "BOOKING_CREATED":
                // no action
                break;
            case "BOOKING_CONFIRM_REQUESTED":
                bookingeventhandeler.handleBookingConfirmRequested(sagaEvent);
                break;
            case "BOOKING_CONFIRMED":
                availabilityhandeler.handleBookingConfirm(sagaEvent);
                log.info("Booking confirmed for booking id: {}", sagaEvent.getPayload().get("bookingId"));
                break;
            case "BOOKING_CANCEL_REQUESTED":
                bookingeventhandeler.handleBookingCancelRequested(sagaEvent);
                break;
            case "BOOKING_CANCELLED":
                availabilityhandeler.handleBookingCancelled(sagaEvent);
                log.info("Booking cancelled for booking id: {}", sagaEvent.getPayload().get("bookingId"));
                break;
            case "BOOKING_COMPENSATED":
                log.info("Booking compensated for booking id: {}", sagaEvent.getPayload().get("bookingId"));
                break;
            default:
                break;
        }
    }
}
