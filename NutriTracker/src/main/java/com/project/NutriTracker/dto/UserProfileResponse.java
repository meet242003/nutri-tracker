package com.project.NutriTracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String id;
    private String name;
    private String email;
    private String profileUrl;
    private Double height;
    private Double weight;
    private LocalDate dateOfBirth;
    private Integer age; // Calculated from dateOfBirth
    private String gender;
    private String activityLevel;
    private String goal;
    private Double bmr; // Basal Metabolic Rate
    private Double tdee; // Total Daily Energy Expenditure
    private NutritionGoals nutritionGoals;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionGoals {
        private Double calories;
        private Double protein; // in grams
        private Double carbohydrates; // in grams
        private Double fat; // in grams
    }
}
