package com.auth.api.controller;

import com.auth.api.dto.ApiResponse;
import com.auth.api.dto.AuthResponse;
import com.auth.api.dto.LoginRequest;
import com.auth.api.dto.RegisterRequest;
import com.auth.api.dto.UserProfileResponse;
import com.auth.api.security.CustomUserDetails;
import com.auth.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserProfileResponse userProfile = authService.register(registerRequest);
        ApiResponse<UserProfileResponse> response = ApiResponse.success(
                "User registered successfully! You can now log in.",
                userProfile
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Login successful!",
                authResponse
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse userProfile = authService.getCurrentUser(userDetails.getUsername());
        ApiResponse<UserProfileResponse> response = ApiResponse.success(
                "Authenticated user details retrieved successfully",
                userProfile
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        ApiResponse<Map<String, String>> response = ApiResponse.success(
                "User Authentication API is operational",
                Map.of("status", "UP", "service", "user-auth-api")
        );
        return ResponseEntity.ok(response);
    }
}
