package com.example.AirbnbBookingSpring.Services;

import com.example.AirbnbBookingSpring.DTOs.CreateBookingRequest;
import com.example.AirbnbBookingSpring.DTOs.UpdateBookingRequest;
import com.example.AirbnbBookingSpring.Saga.SagaEventPublisher;
import com.example.AirbnbBookingSpring.Writes.AirbnbWriteRepository;
import com.example.AirbnbBookingSpring.Writes.AvailabilityWriteRepository;
import com.example.AirbnbBookingSpring.Writes.BookingWriteRepository;
import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Availability;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.reads.RedisWriteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService implements IBookingService{
    private final BookingWriteRepository bookingWriteRepository;
    private final AirbnbWriteRepository airbnbWriteRepository;
    private final AvailabilityWriteRepository availabilityWriteRepository;
    private final ConcurrencyControlStrategy concurrencyControlStrategy;
    private final RedisWriteRepository redisWriteRepository;
    private final IdempotencyService idempotencyService;
    private final SagaEventPublisher sagaEventPublisher;


    @Override
    @Transactional
    public Booking createBooking(CreateBookingRequest createBookingRequest){
                       Airbnb airbnb=airbnbWriteRepository.findById(Long.valueOf(createBookingRequest.getAirbnbId())).orElseThrow(()->new RuntimeException("Airbnb not found"));
                       if(createBookingRequest.getCheckInDate().isAfter(createBookingRequest.getCheckOutDate())){
                           throw new RuntimeException("Check-in date must be before check-out date");
                       }
                       if(createBookingRequest.getCheckInDate().isBefore(LocalDate.now())){
                           throw new RuntimeException("Check-in date must be today or after today");


                       }
        List<Availability> availabilities=concurrencyControlStrategy.lockAndCheckAvailability(
                airbnb.getId(),
                createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate(),
                Long.parseLong(createBookingRequest.getUserId())
        );
                       long nights= ChronoUnit.DAYS.between(createBookingRequest.getCheckInDate(),createBookingRequest.getCheckOutDate());
                       double pricePerNight=airbnb.getPricePerNight();
                       double totalPrice=pricePerNight*nights;
        log.info("Create booking for Airbnb {} with check in date {} and checkout date {} and total price{}",airbnb.getId(),createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate(),totalPrice);
        Booking booking = new Booking().builder()
                .airbnbId(airbnb.getId())
                .userId(createBookingRequest.getUserId())
                .totalPrice(totalPrice)
                .Idempotency(UUID.randomUUID().toString())
                .bookingStatus(Booking.BookingStatus.PENDING)
                .checkInDate(createBookingRequest.getCheckInDate())
                .checkOutDate(createBookingRequest.getCheckOutDate())
                .build();


      booking= bookingWriteRepository.save(booking);
      redisWriteRepository.writeBookingReadModel(booking);

return booking;






    }

    @Override
    @Transactional
    public Booking updateBooking (UpdateBookingRequest updateBookingRequest){
        log.info("Updating booking for idempotency key {}", updateBookingRequest.getIdempotencyKey());
        Booking booking = idempotencyService.findBookingByIdempotencyKey(updateBookingRequest.getIdempotencyKey())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        log.info("Booking found for idempotency key {}", updateBookingRequest.getIdempotencyKey());
        log.info("Booking status: {}", booking.getBookingStatus());
        if(booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not pending");
        }

        if(updateBookingRequest.getBookingstatus() == Booking.BookingStatus.CONFIRMED) { // TODO: This also violates a SOLID principle, please resolve: https://github.com/singhsanket143/AirbnbSpring/issues/13
            sagaEventPublisher.publishEvent("BOOKING_CONFIRM_REQUESTED", "CONFIRM_BOOKING", Map.of("bookingId", booking.getId(), "airbnbId", booking.getAirbnbId(), "CheckInDate", booking.getCheckInDate(), "CheckOutDate", booking.getCheckOutDate()));
        } else if(updateBookingRequest.getBookingstatus() == Booking.BookingStatus.CANCELLED) {
            sagaEventPublisher.publishEvent("BOOKING_CANCEL_REQUESTED", "CANCEL_BOOKING", Map.of("bookingId", booking.getId(), "airbnbId", booking.getAirbnbId(), "CheckInDate", booking.getCheckInDate(), "CheckOutDate", booking.getCheckOutDate()));
        }

        return booking;
    }

}
