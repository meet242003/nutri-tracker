package com.project.NutriTracker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualFoodEntryRequest {
    private List<FoodEntry> foods;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodEntry {
        private String name;
        private Integer quantityGrams;
        private NutritionInfo nutrition; // Optional - if not provided, will search database

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NutritionInfo {
            private Double calories;
            private Double protein;
            private Double carbohydrates;
            private Double fat;
            private Double fiber;
            private Double sugar;
        }
    }
}
