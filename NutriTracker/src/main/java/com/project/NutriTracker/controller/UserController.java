package com.project.NutriTracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.NutriTracker.document.User;
import com.project.NutriTracker.dto.UserProfileRequest;
import com.project.NutriTracker.dto.UserProfileResponse;
import com.project.NutriTracker.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get current user's profile
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            User principal = (User) authentication.getPrincipal();
            String email = principal.getEmail();
            log.info("Fetching profile for user: {}", email);
            UserProfileResponse profile = userService.getUserProfile(email);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch user profile: " + e.getMessage()));
        }
    }

    /**
     * Update current user's profile
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestBody UserProfileRequest request,
            Authentication authentication) {
        try {
            User principal = (User) authentication.getPrincipal();
            String email = principal.getEmail();
            log.info("Updating profile for user: {}", email);

            UserProfileResponse profile = userService.updateUserProfile(email, request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to update user profile: " + e.getMessage()));
        }
    }

    // Inner class for error responses
    private record ErrorResponse(String error) {
    }
}
