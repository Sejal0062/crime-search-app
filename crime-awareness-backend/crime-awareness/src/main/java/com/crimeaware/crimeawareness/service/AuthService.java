package com.crimeaware.crimeawareness.service;

import com.crimeaware.crimeawareness.entity.User;
import com.crimeaware.crimeawareness.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* ================= REGISTER ================= */
    public User register(User user) {
        String email = user.getEmail().trim().toLowerCase();

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        user.setEmail(email);
        user.setPassword(encoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /* ================= LOGIN ================= */
    public boolean login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty()) {
            System.out.println("Login failed: user not found for " + normalizedEmail);
            return false;
        }

        User user = userOpt.get();
        boolean match = encoder.matches(password, user.getPassword());

        System.out.println("Login attempt for " + normalizedEmail + ", password match: " + match);
        return match;
    }
}