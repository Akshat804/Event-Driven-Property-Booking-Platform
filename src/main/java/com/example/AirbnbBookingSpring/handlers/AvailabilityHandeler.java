package com.example.AirbnbBookingSpring.handlers;

import com.example.AirbnbBookingSpring.Saga.SagaEvent;
import com.example.AirbnbBookingSpring.Saga.SagaEventPublisher;
import com.example.AirbnbBookingSpring.Writes.AvailabilityWriteRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvailabilityHandeler {
    private final AvailabilityWriteRepository availabilityrepo;
    private final SagaEventPublisher sagaeventpublisher;
    @Transactional
    public void handleBookingConfirm(SagaEvent sagaevent){
        System.out.println("EVENT = " + sagaevent.getEventType());
        System.out.println("PAYLOAD = " + sagaevent.getPayload());
       try {
           Map<String, Object> payload = sagaevent.getPayload();
           Long bookingId = Long.valueOf(payload.get("bookingId").toString());
           Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
           LocalDate CheckIndate = LocalDate.parse(payload.get("CheckInDate").toString());
           LocalDate CheckOutdate = LocalDate.parse(payload.get("CheckOutDate").toString());

           Long count = availabilityrepo.countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull(airbnbId, CheckIndate, CheckOutdate);
           System.out.println("COUNT = " + count);
           System.out.println("AIRBNB = " + airbnbId);
           System.out.println("CHECKIN = " + CheckIndate);
           System.out.println("CHECKOUT = " + CheckOutdate);
           if (count > 0) {
               sagaeventpublisher.publishEvent("BOOKING_CANCEL_REQUESTED", "CANCEL_BOOKING", payload);
               throw new RuntimeException("BOOKING_CANCEL_REQUESTED as no airbnb for the given date");

           }
           availabilityrepo.updateBookingIdByAirbnbIdAndDateBetween(bookingId, airbnbId, CheckIndate, CheckOutdate);


       }
       catch(Exception e){
           e.printStackTrace();
           Map<String, Object> payload = sagaevent.getPayload();
           sagaeventpublisher.publishEvent("BOOKING_COMPENSATED","COMPENSATE_BOOKING", payload);
           throw new RuntimeException("Failed to confirm booking");
       }



    }
    @Transactional
    public void handleBookingCancelled(SagaEvent sagaevent){
        System.out.println("EVENT = " + sagaevent.getEventType());
        System.out.println("PAYLOAD = " + sagaevent.getPayload());
        try {
            Map<String, Object> payload = sagaevent.getPayload();
            Long bookingId = Long.valueOf(payload.get("bookingId").toString());
            Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
            LocalDate checkInDate = LocalDate.parse(payload.get("CheckInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(payload.get("CheckOutDate").toString());

            availabilityrepo.updateBookingIdByAirbnbIdAndDateBetween(null, airbnbId, checkInDate, checkOutDate);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> payload = sagaevent.getPayload();
            sagaeventpublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to cancel booking", e);
        }
    }

}


