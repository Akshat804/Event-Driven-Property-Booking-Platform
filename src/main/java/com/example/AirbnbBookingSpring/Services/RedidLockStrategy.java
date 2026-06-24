package com.example.AirbnbBookingSpring.Services;

import com.example.AirbnbBookingSpring.Writes.AvailabilityWriteRepository;
import com.example.AirbnbBookingSpring.models.Availability;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Local;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedidLockStrategy implements ConcurrencyControlStrategy {
    private static final String LOCK_KEY_PREFIX="lock:availability";
    private static final Duration LOCKTIMEOUT=Duration.ofMinutes(3);
    private final RedisTemplate<String,String> redisTemplate;
    private final AvailabilityWriteRepository availabilityWriteRepository;

    @Override
    public void releaselock(long airbnbId, LocalDate checkInDate, LocalDate checkOutDate) {
                   String lockKey=generateKey(airbnbId,checkInDate,checkOutDate);
                   String lockValue=redisTemplate.opsForValue().get(lockKey);
                   if(lockValue!=null){
                       redisTemplate.delete(lockKey);

                   }

    }

    @Override
    public List<Availability> lockAndCheckAvailability(long airbnbId, LocalDate checkInDate, LocalDate checkOutDate,Long userId) {
        long bookedSlots=availabilityWriteRepository.countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull(airbnbId, checkInDate, checkOutDate);
        if(bookedSlots>0){
            throw new RuntimeException("Airbnb is not available for all timeline .Please see other dates");

        }
        String lockKey=generateKey(airbnbId,checkInDate,checkOutDate);
        boolean locked=redisTemplate.opsForValue().setIfAbsent(lockKey,lockKey,LOCKTIMEOUT);
        if(!locked){
            System.out.println("Lock already exists!");

        }
        try{
            return availabilityWriteRepository.findByAirbnbIdAndDateBetween(airbnbId,checkInDate,checkOutDate);

        }
        catch(Exception e){
            releaselock(airbnbId,checkInDate,checkOutDate);
            throw e;
        }


    }
    private String generateKey(long airbnbId, LocalDate checkInDate, LocalDate checkOutDate) {
        return LOCK_KEY_PREFIX+airbnbId+checkInDate.toString()+checkOutDate.toString();
    }
}
