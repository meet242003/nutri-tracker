package com.project.NutriTracker.dto;

import java.util.List;

import com.project.NutriTracker.document.MealImage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealAnalysisResponse {
    private String id;
    private String imageUrl;
    private String status;
    private List<MealImage.FoodItem> detectedFoods;
    private MealImage.NutritionSummary nutritionSummary;
    private String uploadedAt;
    private String analyzedAt;
}
