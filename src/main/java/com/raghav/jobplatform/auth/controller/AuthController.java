package com.raghav.jobplatform.auth.controller;

import com.raghav.jobplatform.auth.dto.LoginRequest;
import com.raghav.jobplatform.auth.dto.LoginResponse;
import com.raghav.jobplatform.auth.dto.RegisterRequest;
import com.raghav.jobplatform.auth.dto.UserResponse;
import com.raghav.jobplatform.config.JwtService;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already in use"));
        }

        // Secure password hashing with BCrypt
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());

        UserEntity user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(hashedPassword)
                .role("USER")
                .build();

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElse(null);

        if (user == null || !BCrypt.checkpw(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        }

        String token = jwtService.generateToken(user.getEmail());

        // Create an HttpOnly cookie for session-based security (XSS protection)
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // Set to true in production if running HTTPS
                .path("/")
                .maxAge(24 * 60 * 60) // 1 day
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Return token and profile info in body for headers-based clients
        LoginResponse loginResponse = new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear HttpOnly cookie by setting its maxAge to 0
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
