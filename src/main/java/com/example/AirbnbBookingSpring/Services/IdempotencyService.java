package com.example.AirbnbBookingSpring.Services;

import com.example.AirbnbBookingSpring.Writes.BookingWriteRepository;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.readModles.BookingReadModel;
import com.example.AirbnbBookingSpring.reads.RedisReadRepository;
import com.example.AirbnbBookingSpring.reads.RedisWriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService implements IidempotencyService {
    private final RedisReadRepository  redisReadRepository;
    private final RedisWriteRepository redisWriteRepository;
    private final BookingWriteRepository bookingWriteRepository;

    @Override
    public boolean isIdempotencyKeyUsed(String idempotencyKey)
    {



        return this.findBookingByIdempotencyKey(idempotencyKey).isPresent();
    }
    @Override
    public Optional<Booking>findBookingByIdempotencyKey(String idempotencyKey) {
        BookingReadModel bookingReadModel = redisReadRepository.findBookingByIdempotencyKey(idempotencyKey);
        if (bookingReadModel != null) {
            Booking booking = Booking.builder()
                    .id(bookingReadModel.getId())
                    .airbnbId(bookingReadModel.getAirbnbId())
                    .userId(String.valueOf(bookingReadModel.getUserId()))
                    .totalPrice(bookingReadModel.getTotalPrice())
                    .bookingStatus(Booking.BookingStatus.valueOf(bookingReadModel.getBookingStatus()))
                    .Idempotency(bookingReadModel.getIdempotency())
                    .checkInDate(LocalDate.parse(bookingReadModel.getCheckIndate()))
                    .checkOutDate(LocalDate.parse(bookingReadModel.getCheckOutdate()))
                    .build();
            return Optional.of(booking);

        }
        return bookingWriteRepository.findByIdempotency(idempotencyKey);
    }

}
