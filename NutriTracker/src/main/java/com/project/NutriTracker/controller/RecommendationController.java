package com.project.NutriTracker.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.NutriTracker.document.User;
import com.project.NutriTracker.dto.DietRecommendationResponse;
import com.project.NutriTracker.dto.MonthlyStatsResponse;
import com.project.NutriTracker.dto.UserProfileResponse;
import com.project.NutriTracker.service.GeminiNutritionAnalysisService;
import com.project.NutriTracker.service.StatsService;
import com.project.NutriTracker.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final StatsService statsService;
    private final UserService userService;
    private final GeminiNutritionAnalysisService geminiService;

    @GetMapping("/current")
    public ResponseEntity<?> getDietRecommendation(Authentication authentication) {
        try {
            User principal = (User) authentication.getPrincipal();
            String email = principal.getEmail();

            // Get recent stats (last 7 days including today)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);

            log.info("Generating diet recommendation for user: {} (Range: {} to {})", email, startDate, endDate);

            List<MonthlyStatsResponse.DailyBreakdown> recentStats = statsService.getDailyBreakdownForRange(email,
                    startDate, endDate);

            // Get user profile
            UserProfileResponse userProfile = userService.getUserProfile(email);

            // Generate recommendation
            DietRecommendationResponse recommendation = geminiService.generateDietRecommendation(recentStats,
                    userProfile);

            return ResponseEntity.ok(recommendation);

        } catch (Exception e) {
            log.error("Error generating diet recommendation: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to generate diet recommendation: " + e.getMessage()));
        }
    }

    // Inner class for error responses
    private record ErrorResponse(String error) {
    }
}
