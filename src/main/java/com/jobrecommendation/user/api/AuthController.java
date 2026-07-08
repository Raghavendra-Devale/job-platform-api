package com.jobrecommendation.user.api;

import com.jobrecommendation.user.api.dto.LoginRequest;
import com.jobrecommendation.user.api.dto.LoginResponse;
import com.jobrecommendation.user.api.dto.RegisterRequest;
import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.user.domain.repository.UserRepository;
import com.jobrecommendation.user.infrastructure.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already in use"));
        }

        // Secure password hashing with PasswordEncoder bean
        String hashedPassword = passwordEncoder.encode(request.password());

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
        // Authenticate credentials using standard AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Fetch user from DB (guaranteed to exist since authentication succeeded)
        UserEntity user = userRepository.findByEmail(request.email()).orElseThrow();

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
