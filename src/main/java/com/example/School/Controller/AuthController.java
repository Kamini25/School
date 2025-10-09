package com.example.School.Controller;

import com.example.School.Entity.User;
import com.example.School.Repository.UserRepository;
import com.example.School.Security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username already exists!";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles("USER");
        userRepository.save(user);
        return "User registered successfully!";
    }

    /**
     * AuthenticationManager delegates to the configured DaoAuthenticationProvider.
        It uses the CustomUserDetailsService to load the user from DB.
        It then checks the password using the BCryptPasswordEncoder.
        If valid, the user is authenticated successfully
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginRequest) {
        System.out.println("inside login");
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password"))
        );

        String token = jwtUtil.generateToken(loginRequest.get("username"));
        return Map.of("token", token);
    }
    @GetMapping("/health")
    public String user() {
        return "Hello, authenticated user!";
    }
}
