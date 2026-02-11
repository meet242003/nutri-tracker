package com.project.NutriTracker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodSearchResponse {
    private List<FoodItem> results;
    private Integer totalResults;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodItem {
        private String id;
        private String name;
        private String category;
        private NutritionInfo nutritionPer100g;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
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
