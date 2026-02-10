package com.project.NutriTracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.NutriTracker.dto.LoginRequest;
import com.project.NutriTracker.dto.RegisterRequest;
import com.project.NutriTracker.service.AuthService;
import com.project.NutriTracker.service.CloudStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CloudStorageService cloudStorageService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/send-verification-email")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody String email) {
        authService.sendVerificationEmail(email);
        return ResponseEntity.ok("Verification email sent successfully");
    }

    @GetMapping("/verify-email/{verificationToken}")
    public ResponseEntity<?> verifyEmail(@PathVariable String verificationToken) {
        authService.verifyEmail(verificationToken);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/upload-profile-image")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("file") MultipartFile file) throws Exception {
        String imageUrl = cloudStorageService.uploadFile(file, "profiles");
        return ResponseEntity.ok(imageUrl);
    }
}
