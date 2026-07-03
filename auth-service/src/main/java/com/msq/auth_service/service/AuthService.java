package com.msq.auth_service.service;

import com.msq.auth_service.client.CompanyUserClient;
import com.msq.auth_service.dto.request.LoginRequest;
import com.msq.auth_service.dto.request.RegisterRequest;
import com.msq.auth_service.dto.response.JwtResponse;
import com.msq.auth_service.dto.response.UserResponse;
import com.msq.auth_service.entity.User;
import com.msq.auth_service.repository.UserRepository;
import com.msq.auth_service.security.JwtUtils;
import com.msq.auth_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyUserClient companyUserClient;
    private final EmailService emailService;

    @Transactional
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Generate token with additional claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getId());
        claims.put("companyId", userPrincipal.getCompanyId());
        claims.put("role", userPrincipal.getRole());
        claims.put("fullName", userPrincipal.getFullName());

        String token = jwtUtils.generateToken(claims, userPrincipal.getUsername());

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .username(userPrincipal.getUsername())
                .role(userPrincipal.getRole())
                .userId(userPrincipal.getId())
                .companyId(userPrincipal.getCompanyId())
                .companyName(userPrincipal.getCompanyName())
                .fullName(userPrincipal.getFullName())
                .build();
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setCompanyId(request.getCompanyId());
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);

        // Generate license key if company registration
        if (request.getCompanyId() == null) {
            // This is a company registration request
            // Call Company-User service to create company
            user = companyUserClient.registerCompany(request);
        }

        User savedUser = userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getCompanyId() != null ? "Company" : "New Company"
        );

        return mapToUserResponse(savedUser);
    }

    public JwtResponse refreshToken(String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("companyId", user.getCompanyId());
        claims.put("role", user.getRole());
        claims.put("fullName", user.getFullName());

        String newToken = jwtUtils.generateToken(claims, user.getUsername());

        return JwtResponse.builder()
                .token(newToken)
                .type("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .userId(user.getId())
                .companyId(user.getCompanyId())
                .fullName(user.getFullName())
                .build();
    }

    public void logout(String token) {
        // Invalidate token by adding to blacklist
        // Implementation depends on your token storage strategy
        log.info("User logged out");
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtUtils.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return jwtUtils.validateToken(token, user) && user.getIsActive();
        } catch (Exception e) {
            return false;
        }
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String resetToken = jwtUtils.generatePasswordResetToken(email);
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName(),
                resetToken
        );
    }

    public void resetPassword(String token, String newPassword) {
        String email = jwtUtils.getUsernameFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}