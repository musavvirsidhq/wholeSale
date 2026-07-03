package com.msq.auth_service.controller;

import com.msq.auth_service.dto.request.LoginRequest;
import com.msq.auth_service.dto.request.RegisterRequest;
import com.msq.auth_service.dto.response.ApiResponse;
import com.msq.auth_service.dto.response.JwtResponse;
import com.msq.auth_service.dto.response.UserResponse;
import com.msq.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestHeader("Authorization") String token) {
        JwtResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(isValid, "Token validation completed"));
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset email sent"));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful"));
    }
}