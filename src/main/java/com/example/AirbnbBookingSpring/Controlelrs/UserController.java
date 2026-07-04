package com.example.AirbnbBookingSpring.Controlelrs;

import com.example.AirbnbBookingSpring.DTOs.RegisterUserRequest;
import com.example.AirbnbBookingSpring.DTOs.UserResponse;
import com.example.AirbnbBookingSpring.Services.UserService;
import com.example.AirbnbBookingSpring.models.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        User user = userService.registerUser(registerUserRequest);
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        return ResponseEntity.ok(response);
    }
}
