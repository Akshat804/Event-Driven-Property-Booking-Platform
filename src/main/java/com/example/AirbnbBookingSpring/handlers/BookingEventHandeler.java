package com.example.AirbnbBookingSpring.handlers;

import com.example.AirbnbBookingSpring.Saga.SagaEvent;
import com.example.AirbnbBookingSpring.Saga.SagaEventPublisher;
import com.example.AirbnbBookingSpring.Writes.BookingWriteRepository;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.reads.RedisWriteRepository;
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
            LocalDate CheckIndate = LocalDate.parse(payload.get("CheckInDate").toString());
            LocalDate CheckOutdate = LocalDate.parse(payload.get("CheckOutDate").toString());

            Booking booking = bookingwriterepository.findById(bookingId).orElseThrow(() -> new RuntimeException());
            Booking.BookingStatus previousStatus = booking.getBookingStatus();
            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
            bookingwriterepository.save(booking);
            redisWriteRepository.writeBookingReadModel(booking);
            Map<String, Object> nextPayload = new HashMap<>();
            nextPayload.put("bookingId", bookingId);
            nextPayload.put("airbnbId", airbnbId);
            nextPayload.put("CheckInDate", CheckIndate.toString());
            nextPayload.put("CheckOutDate", CheckOutdate.toString());
            nextPayload.put("previousBookingStatus", previousStatus.name());
            sagaeventpublisher.publishEvent("BOOKING_CONFIRMED", "CONFIRM_BOOKING", nextPayload);
        }
        catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> payload = createCompensationPayload(sagaevent.getPayload(), Booking.BookingStatus.PENDING.name());
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
            Booking.BookingStatus previousStatus = booking.getBookingStatus();
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            bookingwriterepository.save(booking);
            redisWriteRepository.writeBookingReadModel(booking);

            Map<String, Object> nextPayload = new HashMap<>();
            nextPayload.put("bookingId", bookingId);
            nextPayload.put("airbnbId", airbnbId);
            nextPayload.put("CheckInDate", checkInDate.toString());
            nextPayload.put("CheckOutDate", checkOutDate.toString());
            nextPayload.put("previousBookingStatus", previousStatus.name());
            sagaeventpublisher.publishEvent("BOOKING_CANCELLED","CANCEL_BOOKING",
                    nextPayload
            );

        } catch (Exception e) {
            Map<String, Object> payload = createCompensationPayload(sagaevent.getPayload(), Booking.BookingStatus.PENDING.name());
            sagaeventpublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to cancel booking", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBookingCompensated(SagaEvent sagaevent) {
        Map<String, Object> payload = sagaevent.getPayload();
        Long bookingId = Long.valueOf(payload.get("bookingId").toString());
        String status = payload.getOrDefault("compensateBookingStatus",
                payload.getOrDefault("previousBookingStatus", Booking.BookingStatus.PENDING.name())).toString();

        Booking booking = bookingwriterepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for compensation"));
        booking.setBookingStatus(Booking.BookingStatus.valueOf(status));
        bookingwriterepository.save(booking);
        redisWriteRepository.writeBookingReadModel(booking);
        log.info("Booking compensation applied for booking id {} with status {}", bookingId, status);
    }

    private Map<String, Object> createCompensationPayload(Map<String, Object> payload, String fallbackStatus) {
        Map<String, Object> compensationPayload = new HashMap<>(payload);
        compensationPayload.putIfAbsent("compensateBookingStatus",
                payload.getOrDefault("previousBookingStatus", fallbackStatus).toString());
        return compensationPayload;
    }
}
