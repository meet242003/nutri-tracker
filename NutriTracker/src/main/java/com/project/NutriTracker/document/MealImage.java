package com.project.NutriTracker.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "meal_images")
public class MealImage {
    @Id
    private String id;

    private String userId;

    private String imageUrl; // Google Drive URL

    private String fileName;

    private String status; // UPLOADED, PROCESSING, ANALYZED, FAILED

    private List<FoodItem> detectedFoods;

    private NutritionSummary nutritionSummary;

    @CreatedDate
    private LocalDateTime uploadedAt;

    private LocalDateTime analyzedAt;

    private String errorMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodItem {
        private String name;
        private Double confidence; // 0.0 to 1.0
        private Integer quantity; // in grams
        private NutritionInfo nutrition;
        private String visualCues; // Visual reasoning for portion estimate
        private String category; // main_course, bread, dessert, etc.
        private List<IngredientInfo> ingredientBreakdown; // Breakdown from Stage 2
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientInfo {
        private String name;
        private Integer quantityGrams;
        private String category;
        private NutritionInfo nutrition;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionInfo {
        private Double calories;
        private Double protein; // in grams
        private Double carbohydrates; // in grams
        private Double fat; // in grams
        private Double fiber; // in grams
        private Double sugar; // in grams
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionSummary {
        private Double totalCalories;
        private Double totalProtein;
        private Double totalCarbohydrates;
        private Double totalFat;
        private Double totalFiber;
        private Double totalSugar;
    }
}
