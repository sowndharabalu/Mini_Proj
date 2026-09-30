package com.auth.api.service;

import com.auth.api.dto.AuthResponse;
import com.auth.api.dto.LoginRequest;
import com.auth.api.dto.RegisterRequest;
import com.auth.api.dto.UserProfileResponse;

import java.util.List;

public interface AuthService {

    UserProfileResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    UserProfileResponse getCurrentUser(String username);

    List<UserProfileResponse> getAllUsers();

    UserProfileResponse getUserById(Long id);
}
