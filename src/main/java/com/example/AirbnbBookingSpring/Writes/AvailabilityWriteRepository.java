package com.example.AirbnbBookingSpring.Writes;

import com.example.AirbnbBookingSpring.models.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilityWriteRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByAirbnbId(Long airbnbId);
    List<Availability>findByBookingId(Long bookingId);
    List<Availability> findByAirbnbIdAndDateBetween(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate);

    long countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate);
    @Modifying
    @Query("""
UPDATE Availability a
SET a.bookingId = :bookingId
WHERE a.airbnbId = :airbnbId
AND a.date BETWEEN :startDate AND :endDate
""")
    long  updateBookingIdByAirbnbIdAndDateBetween(
            @org.springframework.data.repository.query.Param("bookingId") Long bookingId,
            @org.springframework.data.repository.query.Param("airbnbId") Long airbnbId,
            @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDate endDate
    );

}

