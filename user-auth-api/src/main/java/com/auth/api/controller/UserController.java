package com.auth.api.controller;

import com.auth.api.dto.ApiResponse;
import com.auth.api.dto.UserProfileResponse;
import com.auth.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        List<UserProfileResponse> users = authService.getAllUsers();
        ApiResponse<List<UserProfileResponse>> response = ApiResponse.success(
                "All registered users retrieved successfully",
                users
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long id) {
        UserProfileResponse user = authService.getUserById(id);
        ApiResponse<UserProfileResponse> response = ApiResponse.success(
                "User found successfully",
                user
        );
        return ResponseEntity.ok(response);
    }
}
