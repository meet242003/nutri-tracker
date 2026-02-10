package com.project.NutriTracker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishBreakdown {
    private String dishName;
    private Integer totalPortionGrams;
    private List<Ingredient> ingredients;
    private String cookingMethod;
    private Double confidence;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredient {
        private String name;
        private Integer quantityGrams;
        private String category; // protein, fat, carb, vegetable, dairy, seasoning, etc.
    }
}
