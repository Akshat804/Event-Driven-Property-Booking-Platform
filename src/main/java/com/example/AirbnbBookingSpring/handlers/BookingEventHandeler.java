package com.example.AirbnbBookingSpring.handlers;

import com.example.AirbnbBookingSpring.Saga.SagaEvent;
import com.example.AirbnbBookingSpring.Saga.SagaEventPublisher;
import com.example.AirbnbBookingSpring.Writes.BookingWriteRepository;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.reads.RedisWriteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingEventHandeler {
    private final BookingWriteRepository bookingwriterepository ;
    private final SagaEventPublisher sagaeventpublisher;
    private final RedisWriteRepository redisWriteRepository;
@Transactional

    public void handleBookingConfirmRequested(SagaEvent sagaevent) {
        System.out.println("EVENT = " + sagaevent.getEventType());
        System.out.println("PAYLOAD = " + sagaevent.getPayload());
        try {
            Map<String, Object> payload = sagaevent.getPayload();
            Long bookingId = Long.valueOf(payload.get("bookingId").toString());
            Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
            LocalDate CheckIndate = LocalDate.parse((String) payload.get("CheckInDate"));
            LocalDate CheckOutdate = LocalDate.parse((String) payload.get("CheckOutDate"));

            Booking booking = bookingwriterepository.findById(bookingId).orElseThrow(() -> new RuntimeException());
            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
            bookingwriterepository.save(booking);
            sagaeventpublisher.publishEvent("BOOKING_CONFIRMED", "CONFIRM_BOOKING", Map.of("bookingId", bookingId, "airbnbId", airbnbId, "CheckInDate", CheckIndate, "CheckOutDate", CheckOutdate));
        }
        catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> payload = sagaevent.getPayload();
            sagaeventpublisher.publishEvent("BOOKING_COMPENSATED","COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to confirm booking");

        }
    }
    @Transactional
    public void handleBookingCancelRequested(SagaEvent sagaevent){
        System.out.println("EVENT = " + sagaevent.getEventType());
        System.out.println("PAYLOAD = " + sagaevent.getPayload());
        try {
            Map<String, Object> payload = sagaevent.getPayload();
            Long bookingId = Long.valueOf(payload.get("bookingId").toString());
            Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
            LocalDate checkInDate = LocalDate.parse(payload.get("CheckInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(payload.get("CheckOutDate").toString());

            Booking booking = bookingwriterepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            bookingwriterepository.save(booking);
            redisWriteRepository.writeBookingReadModel(booking);

            sagaeventpublisher.publishEvent("BOOKING_CANCELLED","CANCEL_BOOKING",
                    Map.of("bookingId", bookingId, "airbnbId", airbnbId, "CheckInDate", checkInDate.toString(), "CheckOutDate", checkOutDate.toString())
            );

        } catch (Exception e) {
            Map<String, Object> payload = sagaevent.getPayload();
            sagaeventpublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            // TODO: handle compensation -> add the current booking status also
            throw new RuntimeException("Failed to cancel booking", e);
        }
    }
}
