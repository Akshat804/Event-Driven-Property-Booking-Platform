package com.example.AirbnbBookingSpring.Saga;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SagaEventPublisher {
    private static final String SAGA_QUEUE="saga:events";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    public void publishEvent(String eventType, String Step, Map<String,Object> payload){
        SagaEvent sagaEvent = SagaEvent.builder()
                .eventType(eventType)
                .step(Step)
                .payload(payload)
                .timestamp(LocalDate.now())
                .status(SagaEvent.SagaStatus.PENDING.name())
                .build();
        try{
             String eventJson=objectMapper.writeValueAsString(sagaEvent);
             redisTemplate.opsForList().rightPush(SAGA_QUEUE,eventJson);
        }
        catch (Exception e){
            throw new RuntimeException("Failed to send SagaEvent to redis");
        }

    }
}
