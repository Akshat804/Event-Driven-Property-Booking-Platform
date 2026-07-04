package com.example.AirbnbBookingSpring.Services;

import com.example.AirbnbBookingSpring.DTOs.RegisterUserRequest;
import com.example.AirbnbBookingSpring.Writes.UserWriteRepository;
import com.example.AirbnbBookingSpring.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserWriteRepository userWriteRepository;

    @Transactional
    public User registerUser(RegisterUserRequest registerUserRequest) {
        userWriteRepository.findByEmail(registerUserRequest.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("User already exists with this email");
                });

        User user = User.builder()
                .name(registerUserRequest.getName())
                .email(registerUserRequest.getEmail())
                .password(registerUserRequest.getPassword())
                .build();

        return userWriteRepository.save(user);
    }
}
