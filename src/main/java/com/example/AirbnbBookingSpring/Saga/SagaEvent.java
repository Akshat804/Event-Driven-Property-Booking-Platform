package com.example.AirbnbBookingSpring.Saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SagaEvent implements Serializable {
       private String sagaId;
       private String eventType;
       private String step;
       private Map<String,Object> payload;
       private LocalDate timestamp;
       private String status;
       public enum SagaStatus{
           PENDING,COMPLETED,FAILED,COMPENSATING;
       }
}
