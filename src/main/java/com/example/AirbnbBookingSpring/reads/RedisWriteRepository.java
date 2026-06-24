package com.example.AirbnbBookingSpring.reads;

import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.readModles.BookingReadModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;
@Repository
@RequiredArgsConstructor
public class RedisWriteRepository {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> bookingRedisTemplate;
    public void writeBookingReadModel(Booking booking) {
        BookingReadModel bookingReadModel = new BookingReadModel().builder()
                .id(booking.getId())
                .airbnbId(booking.getAirbnbId())
                 .userId(Long.parseLong(booking.getUserId()))
                .totalPrice((long) booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus().name())
                .idempotency(booking.getIdempotency())
                .CheckIndate(String.valueOf(booking.getCheckInDate()))
                .CheckOutdate(String.valueOf(booking.getCheckOutDate()))
                .build();
        saveBookingReadModel(bookingReadModel);
    }
    public void saveBookingReadModel(BookingReadModel bookingreadmodel) {
        String key=RedisReadRepository.BOOKING_KEY_PREFIX+ bookingreadmodel.getId();

        String value=objectMapper.writeValueAsString(bookingreadmodel);
        bookingRedisTemplate.opsForValue().set(key, value);


    }
}
