package com.example.AirbnbBookingSpring.handlers;

import com.example.AirbnbBookingSpring.Saga.SagaEvent;
import com.example.AirbnbBookingSpring.Saga.SagaEventPublisher;
import com.example.AirbnbBookingSpring.Writes.AvailabilityWriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
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
               Map<String, Object> compensationPayload = createCompensationPayload(payload);
               sagaeventpublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", compensationPayload);
               throw new RuntimeException("BOOKING_COMPENSATED as no airbnb for the given date");

           }
           availabilityrepo.updateBookingIdByAirbnbIdAndDateBetween(bookingId, airbnbId, CheckIndate, CheckOutdate);


       }
       catch(Exception e){
           e.printStackTrace();
           if (!"BOOKING_COMPENSATED as no airbnb for the given date".equals(e.getMessage())) {
               Map<String, Object> payload = createCompensationPayload(sagaevent.getPayload());
               sagaeventpublisher.publishEvent("BOOKING_COMPENSATED","COMPENSATE_BOOKING", payload);
           }
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
            Map<String, Object> payload = createCompensationPayload(sagaevent.getPayload());
            payload.put("compensateAvailabilityBookingId", sagaevent.getPayload().get("bookingId"));
            sagaeventpublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to cancel booking", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAvailabilityCompensated(SagaEvent sagaevent) {
        Map<String, Object> payload = sagaevent.getPayload();
        if (!payload.containsKey("compensateAvailabilityBookingId")) {
            return;
        }

        Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
        LocalDate checkInDate = LocalDate.parse(payload.get("CheckInDate").toString());
        LocalDate checkOutDate = LocalDate.parse(payload.get("CheckOutDate").toString());
        Object bookingIdValue = payload.get("compensateAvailabilityBookingId");
        Long bookingId = bookingIdValue == null ? null : Long.valueOf(bookingIdValue.toString());

        availabilityrepo.updateBookingIdByAirbnbIdAndDateBetween(bookingId, airbnbId, checkInDate, checkOutDate);
        log.info("Availability compensation applied for airbnb id {} from {} to {}", airbnbId, checkInDate, checkOutDate);
    }

    private Map<String, Object> createCompensationPayload(Map<String, Object> payload) {
        Map<String, Object> compensationPayload = new HashMap<>(payload);
        compensationPayload.putIfAbsent("compensateBookingStatus",
                payload.getOrDefault("previousBookingStatus", "PENDING").toString());
        return compensationPayload;
    }

}


