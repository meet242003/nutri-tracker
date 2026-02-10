package com.project.NutriTracker.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.NutriTracker.document.User;
import com.project.NutriTracker.dto.AuthResponse;
import com.project.NutriTracker.dto.LoginRequest;
import com.project.NutriTracker.dto.RegisterRequest;
import com.project.NutriTracker.exception.ResourceExistsException;
import com.project.NutriTracker.exception.ResourceNotFoundException;
import com.project.NutriTracker.repository.UserRepository;
import com.project.NutriTracker.utils.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MailService mailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Value("${app.base.url}")
    private String appBaseUrl;

    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new ResourceExistsException("User with email " + registerRequest.getEmail() + " already exists");
        }
        User user = toUser(registerRequest);
        userRepository.save(user);
        sendVerificationEmail(user.getEmail());
        return mapUserToAuthResponse(user);
    }

    private AuthResponse mapUserToAuthResponse(User user) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setId(user.getId());
        authResponse.setName(user.getName());
        authResponse.setEmail(user.getEmail());
        authResponse.setProfileUrl(user.getProfileUrl());
        authResponse.setEmailVerified(user.isEmailVerified());
        return authResponse;
    }

    private User toUser(RegisterRequest registerRequest) {
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setProfileUrl(registerRequest.getProfileUrl());
        user.setEmailVerified(false);
        return user;
    }

    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User with email " + loginRequest.getEmail() + " not found");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified");
        }
        AuthResponse response = mapUserToAuthResponse(user);
        response.setToken(jwtUtil.generateToken(user.getId()));
        return response;
    }

    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User with email " + email + " not found");
        }
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusDays(1));
        userRepository.save(user);
        try {
            String verificationUrl = appBaseUrl + "/api/auth/verify-email/" + verificationToken;
            String subject = "Verify your email address";
            String content = "Please click the link below to verify your email: <a href='" + verificationUrl
                    + "'>Verify Email</a>";
            mailService.sendHtmlEmail(email, subject, content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email");
        }
    }

    public boolean verifyEmail(String verificationToken) {
        User user = userRepository.findByEmailVerificationToken(verificationToken).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User with verification token " + verificationToken + " not found");
        }
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
        return true;
    }
}
