package com.project.NutriTracker.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.NutriTracker.document.MealImage;
import com.project.NutriTracker.dto.MealAnalysisResponse;
import com.project.NutriTracker.dto.MealImageUploadResponse;
import com.project.NutriTracker.service.MealImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
@Slf4j
public class MealImageController {

    private final MealImageService mealImageService;

    /**
     * Upload meal image for analysis
     * POST /api/meals/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMealImage(
            @RequestParam("image") MultipartFile image,
            Authentication authentication) {

        try {
            log.info("Received meal image upload request from user: {}", authentication.getName());

            // Get user ID from authentication
            String userId = authentication.getName(); // This should be the user's email or ID

            // Upload and process image
            MealImageUploadResponse response = mealImageService.uploadMealImage(image, userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading meal image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to upload image: " + e.getMessage()));
        }
    }

    /**
     * Get all meal images for authenticated user
     * GET /api/meals
     */
    @GetMapping
    public ResponseEntity<?> getUserMealImages(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<MealImage> mealImages = mealImageService.getUserMealImages(userId);
            return ResponseEntity.ok(mealImages);
        } catch (Exception e) {
            log.error("Error fetching meal images: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch meal images"));
        }
    }

    /**
     * Get meal image by ID
     * GET /api/meals/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMealImageById(
            @PathVariable String id,
            Authentication authentication) {
        try {
            MealImage mealImage = mealImageService.getMealImageById(id);

            // Verify user owns this meal image
            if (!mealImage.getUserId().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Access denied"));
            }

            return ResponseEntity.ok(mealImage);
        } catch (Exception e) {
            log.error("Error fetching meal image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Meal image not found"));
        }
    }

    /**
     * Get meal analysis by ID
     * GET /api/meals/{id}/analysis
     */
    @GetMapping("/{id}/analysis")
    public ResponseEntity<?> getMealAnalysis(
            @PathVariable String id,
            Authentication authentication) {
        try {
            MealImage mealImage = mealImageService.getMealImageById(id);

            // Verify user owns this meal image
            if (!mealImage.getUserId().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Access denied"));
            }

            MealAnalysisResponse response = mealImageService.getMealAnalysis(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching meal analysis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Meal analysis not found"));
        }
    }

    /**
     * Delete meal image
     * DELETE /api/meals/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMealImage(
            @PathVariable String id,
            Authentication authentication) {
        try {
            MealImage mealImage = mealImageService.getMealImageById(id);

            // Verify user owns this meal image
            if (!mealImage.getUserId().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Access denied"));
            }

            mealImageService.deleteMealImage(id);
            return ResponseEntity.ok(new SuccessResponse("Meal image deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting meal image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete meal image"));
        }
    }

    // Inner classes for responses
    private record ErrorResponse(String error) {
    }

    private record SuccessResponse(String message) {
    }
}
