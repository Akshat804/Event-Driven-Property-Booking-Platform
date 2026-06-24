package com.example.AirbnbBookingSpring.Writes;

import com.example.AirbnbBookingSpring.models.Airbnb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirbnbWriteRepository extends JpaRepository<Airbnb,Long> {

}
