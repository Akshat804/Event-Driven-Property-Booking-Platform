package com.example.AirbnbBookingSpring.reads;

import com.example.AirbnbBookingSpring.readModles.AirbnbReadModel;
import com.example.AirbnbBookingSpring.readModles.AvailabilityReadModel;
import com.example.AirbnbBookingSpring.readModles.BookingReadModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisReadRepository {
    private static final String  AIRBNB_KEY_PREFIX="airbnb";
    public static final String BOOKING_KEY_PREFIX="booking";
    private static final String AVAILABILITY_KEY_PREFIX="availability";
   private final  RedisTemplate<String,String>redisTemplate;
   private final ObjectMapper objectMapper;

   public AirbnbReadModel findAirbnbById(Long id){
       String key = AIRBNB_KEY_PREFIX+id;
       String value = redisTemplate.opsForValue().get(key);
       if(value == null){
           return null;
       }
       try{
           return objectMapper.readValue(value, AirbnbReadModel.class);

       }
       catch(Exception e){
           throw new RuntimeException(e);
       }

   }
    public BookingReadModel findBookingById(Long id){
        String key = BOOKING_KEY_PREFIX+id;
        String value = redisTemplate.opsForValue().get(key);
        if(value == null){
            return null;
        }
        try{
            return objectMapper.readValue(value, BookingReadModel.class);

        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

    }
    public AvailabilityReadModel findAvailabilityById(Long id){
        String key = AVAILABILITY_KEY_PREFIX+id;
        String value = redisTemplate.opsForValue().get(key);
        if(value == null){
            return null;
        }
        try{
            return objectMapper.readValue(value, AvailabilityReadModel.class);

        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

    }
    public List<AirbnbReadModel> findAllAirbnbs() {
        Set<String> keys = redisTemplate.keys(AIRBNB_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return keys.stream()
                .map(key -> {
                    String value = redisTemplate.opsForValue().get(key);

                    if (value == null) {
                        return null;
                    }

                    try {
                        return objectMapper.readValue(value, AirbnbReadModel.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public BookingReadModel findBookingByIdempotencyKey(String idempotencyKey){
       Set<String>keys=redisTemplate.keys(BOOKING_KEY_PREFIX+"*");
       if(keys.isEmpty() || keys == null){
           return null;
       }
       return keys.stream()
               .map(key -> {
                   String value=redisTemplate.opsForValue().get(key);
                   if(value!=null){
                       try {
                           BookingReadModel bookingReadModel = objectMapper.readValue(value, BookingReadModel.class);
                           if (idempotencyKey.equals(bookingReadModel.getIdempotency())) {
                               return bookingReadModel;
                           }
                       }
                           catch(JacksonException e){
                               throw new RuntimeException("Failed to parse booking read model from redis",e);

                           }
                       }
                       return null;


               })
               .filter(booking -> booking!=null)
               .findFirst()
               .orElse(null);
    }






}
