package com.example.citizen_backend.controller;

import com.example.citizen_backend.model.User;
import com.example.citizen_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // ✅ Register new user
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return "❌ Username already taken!";
        }

        userRepository.save(user);
        return "✅ User registered successfully!";
    }

    // ✅ Login user
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser.isPresent()) {
            if (existingUser.get().getPassword().equals(user.getPassword())) {
                return "✅ Login successful!";
            } else {
                return "❌ Incorrect password!";
            }
        }

        return "❌ User not found!";
    }
}