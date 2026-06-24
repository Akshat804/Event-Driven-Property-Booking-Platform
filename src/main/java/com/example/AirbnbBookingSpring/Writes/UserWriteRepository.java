package com.example.AirbnbBookingSpring.Writes;

import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWriteRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

}
