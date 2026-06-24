package com.example.AirbnbBookingSpring.Writes;

import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Booking;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingWriteRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByAirbnbId(String airbnbId);
  //  @Lock(LockModeType.PESSIMISTIC_WRITE)
  //  @Query("Select b from Booking b where b.id=:id")
  //  Optional<Booking> findByIdwithLock(@Param("id") Long id);
  Optional<Booking> findByIdempotency(String idempotencyKey);

}

