package com.project.NutriTracker.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.protobuf.ByteString;
import com.project.NutriTracker.document.MealImage;
import com.project.NutriTracker.document.MealImage.FoodItem;
import com.project.NutriTracker.document.MealImage.NutritionInfo;
import com.project.NutriTracker.document.MealImage.NutritionSummary;
import com.project.NutriTracker.document.FoodComposition;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleVisionService {

    private final NutritionDatabaseService nutritionDatabaseService;

    private GoogleCredentials credentials;

    @PostConstruct
    public void init() {
        try {
            // Load credentials from vision-account-key.json
            ClassPathResource resource = new ClassPathResource("vision-account-key.json");
            this.credentials = GoogleCredentials.fromStream(resource.getInputStream());
            log.info("Successfully initialized Google Vision API credentials");
        } catch (Exception e) {
            log.error("Failed to load Google Vision API credentials from vision-account-key.json", e);
        }
    }

    public MealImage analyzeMeal(byte[] imageBytes, String mimeType, String location) throws IOException {
        if (this.credentials == null) {
            throw new IllegalStateException("Google Vision API credentials not configured");
        }

        // Create settings with credentials
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings)) {

            // Build the image
            ByteString imgBytes = ByteString.copyFrom(imageBytes);
            Image img = Image.newBuilder().setContent(imgBytes).build();

            // Configure features - using LABEL_DETECTION and OBJECT_LOCALIZATION for food
            // detection
            List<Feature> features = new ArrayList<>();
            features.add(Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).setMaxResults(20).build());
            features.add(Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).setMaxResults(20).build());

            // Build the request
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addAllFeatures(features)
                    .setImage(img)
                    .build();

            // Perform the request
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            List<AnnotateImageResponse> responses = response.getResponsesList();

            if (responses.isEmpty()) {
                throw new IOException("No response from Vision API");
            }

            AnnotateImageResponse imageResponse = responses.get(0);

            if (imageResponse.hasError()) {
                throw new IOException("Vision API error: " + imageResponse.getError().getMessage());
            }

            // Process labels and objects to identify food items
            List<FoodItem> detectedFoods = new ArrayList<>();
            Double totalCalories = 0.0;
            Double totalProtein = 0.0;
            Double totalCarbs = 0.0;
            Double totalFat = 0.0;
            Double totalFiber = 0.0;

            // Get labels (food categories)
            List<EntityAnnotation> labels = imageResponse.getLabelAnnotationsList();
            log.info("Detected {} labels", labels.size());

            // Get localized objects (specific food items)
            List<com.google.cloud.vision.v1.LocalizedObjectAnnotation> objects = imageResponse
                    .getLocalizedObjectAnnotationsList();
            log.info("Detected {} objects", objects.size());

            // Process localized objects first (more specific)
            for (com.google.cloud.vision.v1.LocalizedObjectAnnotation object : objects) {
                String name = object.getName();
                float confidence = object.getScore();

                // Filter for food-related objects
                if (isFoodRelated(name)) {
                    FoodItem foodItem = createFoodItem(name, confidence);
                    if (foodItem != null) {
                        detectedFoods.add(foodItem);

                        // Aggregate nutrition
                        if (foodItem.getNutrition() != null) {
                            totalCalories += foodItem.getNutrition().getCalories();
                            totalProtein += foodItem.getNutrition().getProtein();
                            totalCarbs += foodItem.getNutrition().getCarbohydrates();
                            totalFat += foodItem.getNutrition().getFat();
                            totalFiber += foodItem.getNutrition().getFiber();
                        }
                    }
                }
            }

            // If no objects detected, use labels
            if (detectedFoods.isEmpty()) {
                for (EntityAnnotation label : labels) {
                    String name = label.getDescription();
                    float confidence = label.getScore();

                    // Filter for food-related labels
                    if (isFoodRelated(name) && confidence > 0.7) {
                        FoodItem foodItem = createFoodItem(name, confidence);
                        if (foodItem != null) {
                            detectedFoods.add(foodItem);

                            // Aggregate nutrition
                            if (foodItem.getNutrition() != null) {
                                totalCalories += foodItem.getNutrition().getCalories();
                                totalProtein += foodItem.getNutrition().getProtein();
                                totalCarbs += foodItem.getNutrition().getCarbohydrates();
                                totalFat += foodItem.getNutrition().getFat();
                                totalFiber += foodItem.getNutrition().getFiber();
                            }
                        }

                        // Limit to top 10 food items
                        if (detectedFoods.size() >= 10) {
                            break;
                        }
                    }
                }
            }

            // Create result
            MealImage result = new MealImage();
            result.setDetectedFoods(detectedFoods);

            NutritionSummary summary = new NutritionSummary();
            summary.setTotalCalories(formatDouble(totalCalories));
            summary.setTotalProtein(formatDouble(totalProtein));
            summary.setTotalCarbohydrates(formatDouble(totalCarbs));
            summary.setTotalFat(formatDouble(totalFat));
            summary.setTotalFiber(formatDouble(totalFiber));
            summary.setTotalSugar(0.0); // Not calculated yet

            result.setNutritionSummary(summary);

            log.info("Analysis complete. Detected {} food items", detectedFoods.size());
            return result;
        }
    }

    private boolean isFoodRelated(String label) {
        String lowerLabel = label.toLowerCase();
        // Common food-related keywords
        return lowerLabel.contains("food") ||
                lowerLabel.contains("dish") ||
                lowerLabel.contains("meal") ||
                lowerLabel.contains("cuisine") ||
                lowerLabel.contains("rice") ||
                lowerLabel.contains("bread") ||
                lowerLabel.contains("curry") ||
                lowerLabel.contains("dal") ||
                lowerLabel.contains("roti") ||
                lowerLabel.contains("naan") ||
                lowerLabel.contains("vegetable") ||
                lowerLabel.contains("meat") ||
                lowerLabel.contains("chicken") ||
                lowerLabel.contains("fish") ||
                lowerLabel.contains("fruit") ||
                lowerLabel.contains("dessert") ||
                lowerLabel.contains("sweet") ||
                lowerLabel.contains("snack") ||
                lowerLabel.contains("drink") ||
                lowerLabel.contains("beverage");
    }

    private FoodItem createFoodItem(String name, float confidence) {
        FoodItem foodItem = new FoodItem();
        foodItem.setName(name);
        foodItem.setConfidence((double) confidence);

        // Estimate portion size (default 150g for main dishes, 100g for others)
        Integer estimatedQuantity = estimatePortionSize(name);
        foodItem.setQuantity(estimatedQuantity);

        // Calculate nutrition
        NutritionInfo nutrition = calculateNutrition(name, estimatedQuantity);
        foodItem.setNutrition(nutrition);

        return foodItem;
    }

    private Integer estimatePortionSize(String foodName) {
        String lower = foodName.toLowerCase();

        // Estimate based on food type
        if (lower.contains("rice") || lower.contains("curry") || lower.contains("dal")) {
            return 150; // 150g for main dishes
        } else if (lower.contains("roti") || lower.contains("naan") || lower.contains("bread")) {
            return 50; // 50g for bread items
        } else if (lower.contains("vegetable") || lower.contains("salad")) {
            return 100; // 100g for vegetables
        } else if (lower.contains("sweet") || lower.contains("dessert")) {
            return 75; // 75g for desserts
        } else {
            return 100; // Default 100g
        }
    }

    private NutritionInfo calculateNutrition(String foodName, Integer quantityGrams) {
        // Look up in database
        FoodComposition food = nutritionDatabaseService.findFoodByName(foodName);

        if (food != null) {
            double factor = quantityGrams / 100.0;
            NutritionInfo info = new NutritionInfo();
            info.setCalories(formatDouble(food.getEnergyKcal() * factor));
            info.setProtein(formatDouble(food.getProtein() * factor));
            info.setCarbohydrates(formatDouble(food.getCarbohydrate() * factor));
            info.setFat(formatDouble(food.getTotalFat() * factor));
            info.setFiber(formatDouble(food.getTotalFiber() * factor));
            info.setSugar(0.0); // Not in CSV
            return info;
        }

        // Fallback: If not found, return zeroes
        NutritionInfo empty = new NutritionInfo();
        empty.setCalories(0.0);
        empty.setProtein(0.0);
        empty.setCarbohydrates(0.0);
        empty.setFat(0.0);
        empty.setFiber(0.0);
        empty.setSugar(0.0);
        return empty;
    }

    private Double formatDouble(Double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
