package com.project.NutriTracker.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.NutriTracker.document.MealImage;
import com.project.NutriTracker.dto.MealAnalysisResponse;
import com.project.NutriTracker.dto.MealImageUploadResponse;
import com.project.NutriTracker.exception.ResourceNotFoundException;
import com.project.NutriTracker.repository.MealImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealImageService {

    private final MealImageRepository mealImageRepository;
    private final CloudStorageService cloudStorageService;
    private final AsyncMealAnalysisService asyncMealAnalysisService;

    /**
     * Upload meal image and save metadata
     */
    public MealImageUploadResponse uploadMealImage(MultipartFile file, String userId) {
        try {
            log.info("Uploading meal image for user: {}", userId);

            // Validate file
            validateImageFile(file);

            // Read bytes for analysis
            byte[] imageBytes = file.getBytes();
            String contentType = file.getContentType();

            // Upload to Google Cloud Storage
            String imageUrl = cloudStorageService.uploadFile(file, "meals");

            // Create meal image document
            MealImage mealImage = new MealImage();
            mealImage.setUserId(userId);
            mealImage.setImageUrl(imageUrl);
            mealImage.setFileName(file.getOriginalFilename());
            mealImage.setStatus("UPLOADED");
            mealImage.setUploadedAt(LocalDateTime.now());

            // Save to database
            MealImage savedImage = mealImageRepository.save(mealImage);

            log.info("Meal image uploaded successfully with ID: {} for userId: {}", savedImage.getId(), userId);

            // Trigger async analysis
            // We use the bytes we read so we don't depend on the MultipartFile/stream
            // validity in the async thread
            asyncMealAnalysisService.analyzeImageAsync(savedImage.getId(), imageBytes, contentType);

            // Return response
            return new MealImageUploadResponse(
                    savedImage.getId(),
                    savedImage.getImageUrl(),
                    savedImage.getStatus(),
                    "Image uploaded successfully. Analysis will be performed shortly.",
                    savedImage.getUploadedAt().toString());

        } catch (Exception e) {
            log.error("Error uploading meal image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload meal image: " + e.getMessage());
        }
    }

    /**
     * Get meal image by ID
     */
    public MealImage getMealImageById(String id) {
        return mealImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal image not found with ID: " + id));
    }

    /**
     * Get all meal images for a user
     */
    public List<MealImage> getUserMealImages(String userId) {
        return mealImageRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    /**
     * Get meal analysis by ID
     */
    public MealAnalysisResponse getMealAnalysis(String id) {
        MealImage mealImage = getMealImageById(id);

        return new MealAnalysisResponse(
                mealImage.getId(),
                mealImage.getImageUrl(),
                mealImage.getStatus(),
                mealImage.getDetectedFoods(),
                mealImage.getNutritionSummary(),
                mealImage.getUploadedAt() != null ? mealImage.getUploadedAt().toString() : null,
                mealImage.getAnalyzedAt() != null ? mealImage.getAnalyzedAt().toString() : null);
    }

    /**
     * Update meal image status
     */
    public void updateMealImageStatus(String id, String status) {
        MealImage mealImage = getMealImageById(id);
        mealImage.setStatus(status);
        mealImageRepository.save(mealImage);
    }

    /**
     * Delete meal image
     */
    public void deleteMealImage(String id) {
        MealImage mealImage = getMealImageById(id);
        mealImageRepository.delete(mealImage);
        log.info("Deleted meal image with ID: {}", id);
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate supported formats
        List<String> supportedFormats = List.of("image/jpeg", "image/jpg", "image/png", "image/webp");
        if (!supportedFormats.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image format. Supported formats: JPEG, JPG, PNG, WEBP");
        }
    }

    /**
     * Create manual food entry without image
     */
    public MealImage createManualFoodEntry(String userId,
            List<com.project.NutriTracker.dto.ManualFoodEntryRequest.FoodEntry> foods) {
        try {
            log.info("Creating manual food entry for user: {}", userId);

            // Create meal image document without image URL
            MealImage mealImage = new MealImage();
            mealImage.setUserId(userId);
            mealImage.setImageUrl(null); // No image for manual entry
            mealImage.setFileName("Manual Entry");
            mealImage.setStatus("ANALYZED"); // Already analyzed since user provided data
            mealImage.setUploadedAt(LocalDateTime.now());
            mealImage.setAnalyzedAt(LocalDateTime.now());

            // Convert food entries to detected foods
            List<MealImage.FoodItem> detectedFoods = foods.stream()
                    .map(food -> {
                        MealImage.FoodItem foodItem = new MealImage.FoodItem();
                        foodItem.setName(food.getName());
                        foodItem.setQuantity(food.getQuantityGrams());
                        foodItem.setConfidence(1.0); // Manual entry is 100% confident

                        // Convert nutrition info
                        if (food.getNutrition() != null) {
                            MealImage.NutritionInfo nutrition = new MealImage.NutritionInfo();
                            nutrition.setCalories(food.getNutrition().getCalories());
                            nutrition.setProtein(food.getNutrition().getProtein());
                            nutrition.setCarbohydrates(food.getNutrition().getCarbohydrates());
                            nutrition.setFat(food.getNutrition().getFat());
                            nutrition.setFiber(food.getNutrition().getFiber());
                            nutrition.setSugar(food.getNutrition().getSugar());
                            foodItem.setNutrition(nutrition);
                        }

                        return foodItem;
                    })
                    .toList();

            mealImage.setDetectedFoods(detectedFoods);

            // Calculate nutrition summary
            MealImage.NutritionSummary summary = calculateNutritionSummary(detectedFoods);
            mealImage.setNutritionSummary(summary);

            // Save to database
            MealImage savedImage = mealImageRepository.save(mealImage);
            log.info("Manual food entry created successfully with ID: {}", savedImage.getId());

            return savedImage;

        } catch (Exception e) {
            log.error("Error creating manual food entry: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create manual food entry: " + e.getMessage());
        }
    }

    /**
     * Calculate nutrition summary from food items
     */
    private MealImage.NutritionSummary calculateNutritionSummary(List<MealImage.FoodItem> foods) {
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        double totalFiber = 0;
        double totalSugar = 0;

        for (MealImage.FoodItem food : foods) {
            if (food.getNutrition() != null) {
                MealImage.NutritionInfo nutrition = food.getNutrition();
                totalCalories += nutrition.getCalories() != null ? nutrition.getCalories() : 0;
                totalProtein += nutrition.getProtein() != null ? nutrition.getProtein() : 0;
                totalCarbs += nutrition.getCarbohydrates() != null ? nutrition.getCarbohydrates() : 0;
                totalFat += nutrition.getFat() != null ? nutrition.getFat() : 0;
                totalFiber += nutrition.getFiber() != null ? nutrition.getFiber() : 0;
                totalSugar += nutrition.getSugar() != null ? nutrition.getSugar() : 0;
            }
        }

        MealImage.NutritionSummary summary = new MealImage.NutritionSummary();
        summary.setTotalCalories(totalCalories);
        summary.setTotalProtein(totalProtein);
        summary.setTotalCarbohydrates(totalCarbs);
        summary.setTotalFat(totalFat);
        summary.setTotalFiber(totalFiber);
        summary.setTotalSugar(totalSugar);

        return summary;
    }
}
