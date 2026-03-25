package com.crimeaware.crimeawareness.controller;

import com.crimeaware.crimeawareness.entity.User;
import com.crimeaware.crimeawareness.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        boolean success = authService.login(user.getEmail(), user.getPassword());

        if (success) {
            return "Login Successful";
        } else {
            return "Invalid credentials";
        }
    }
}