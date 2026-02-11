package com.project.NutriTracker.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStatsResponse {
    private LocalDate date;
    private NutritionConsumed consumed;
    private NutritionGoals goals;
    private NutritionRemaining remaining;
    private List<MealSummary> meals;
    private Integer totalMeals;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionConsumed {
        private Double calories;
        private Double protein;
        private Double carbohydrates;
        private Double fat;
        private Double fiber;
        private Double sugar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionGoals {
        private Double calories;
        private Double protein;
        private Double carbohydrates;
        private Double fat;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionRemaining {
        private Double calories;
        private Double protein;
        private Double carbohydrates;
        private Double fat;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MealSummary {
        private String id;
        private String imageUrl;
        private String uploadedAt;
        private NutritionConsumed nutrition;
        private List<String> foodItems;
    }
}
