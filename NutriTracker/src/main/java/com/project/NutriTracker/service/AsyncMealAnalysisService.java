package com.project.NutriTracker.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.project.NutriTracker.document.MealImage;
import com.project.NutriTracker.repository.MealImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncMealAnalysisService {

    private final MealImageRepository mealImageRepository;
    private final GeminiNutritionAnalysisService geminiNutritionAnalysisService;

    @Async
    public void analyzeImageAsync(String mealImageId, byte[] imageBytes, String mimeType) {
        log.info("Starting async two-stage nutrition analysis for meal image: {}", mealImageId);
        try {
            // Update status to PROCESSING
            updateStatus(mealImageId, "PROCESSING");

            // Call Gemini Nutrition Analysis (Two-Stage Pipeline)
            MealImage analysisResult = geminiNutritionAnalysisService.analyzeMeal(imageBytes, mimeType);

            // Update DB record
            MealImage mealImage = mealImageRepository.findById(mealImageId).orElse(null);
            if (mealImage != null) {
                mealImage.setDetectedFoods(analysisResult.getDetectedFoods());
                mealImage.setNutritionSummary(analysisResult.getNutritionSummary());
                mealImage.setStatus("ANALYZED");
                mealImage.setAnalyzedAt(LocalDateTime.now());
                mealImageRepository.save(mealImage);
                log.info("Analysis completed for meal image: {}", mealImageId);
            } else {
                log.warn("Meal image not found for analysis: {}", mealImageId);
            }

        } catch (Exception e) {
            log.error("Error analyzing meal image: {}", mealImageId, e);
            updateStatus(mealImageId, "FAILED", e.getMessage());
        }
    }

    private void updateStatus(String id, String status) {
        updateStatus(id, status, null);
    }

    private void updateStatus(String id, String status, String error) {
        mealImageRepository.findById(id).ifPresent(img -> {
            img.setStatus(status);
            if (error != null) {
                img.setErrorMessage(error);
            }
            mealImageRepository.save(img);
        });
    }
}
