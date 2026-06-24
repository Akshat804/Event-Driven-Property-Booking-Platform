package com.example.AirbnbBookingSpring.Saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventConsumer {
   private final String SAGA_QUEUE="saga:events";
   private final RedisTemplate<String,String> redisTemplate;
   private final ObjectMapper objectMapper;
   private final SagaEventProcessor sagaEventProcessor;
    @Scheduled(fixedDelay =500)
    public void consumeEvents(){
        String eventJson = redisTemplate.opsForList().leftPop(SAGA_QUEUE,1, TimeUnit.SECONDS);
        if(eventJson!=null && !eventJson.isEmpty()){
            try {
                 SagaEvent sagaEvent = objectMapper.readValue(eventJson,SagaEvent.class);
                 sagaEventProcessor.processEvent(sagaEvent);
                 log.info("Saga event processed done saga id {}",sagaEvent.getSagaId());
            }
            catch (Exception e){
                log.info("Saga event processed failed");
                throw new RuntimeException("failed to process saga");
            }
        }


   }

}
