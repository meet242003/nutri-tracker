package com.project.NutriTracker.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.project.NutriTracker.document.FoodComposition;
import com.project.NutriTracker.document.MealImage;
import com.project.NutriTracker.document.MealImage.FoodItem;
import com.project.NutriTracker.document.MealImage.IngredientInfo;
import com.project.NutriTracker.document.MealImage.NutritionInfo;
import com.project.NutriTracker.document.MealImage.NutritionSummary;
import com.project.NutriTracker.dto.DetectedDish;
import com.project.NutriTracker.dto.DietRecommendationResponse;
import com.project.NutriTracker.dto.DishBreakdown;
import com.project.NutriTracker.dto.MonthlyStatsResponse;
import com.project.NutriTracker.dto.UserProfileResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiNutritionAnalysisService {

    private final NutritionDatabaseService nutritionDatabaseService;
    private final ObjectMapper objectMapper;

    private String modelName = "gemini-2.0-flash";

    @Value("${app.gemini.api.key}")
    private String apiKey;

    private Client client;

    @PostConstruct
    public void init() {
        this.client = Client.builder().apiKey(apiKey).build();
        log.info("Initialized Google GenAI client with model: {}", modelName);
    }

    // Generic nutrition values for common categories (per 100g)
    private static final Map<String, NutritionInfo> GENERIC_NUTRITION = new HashMap<>();

    static {
        // Fallback values for items not in database
        GENERIC_NUTRITION.put("spices", createNutritionInfo(5.0, 0.2, 1.0, 0.1, 0.5, 0.0));
        GENERIC_NUTRITION.put("water", createNutritionInfo(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        GENERIC_NUTRITION.put("salt", createNutritionInfo(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        GENERIC_NUTRITION.put("oil", createNutritionInfo(884.0, 0.0, 0.0, 100.0, 0.0, 0.0));
    }

    private static NutritionInfo createNutritionInfo(Double cal, Double prot, Double carb, Double fat, Double fib,
            Double sug) {
        NutritionInfo info = new NutritionInfo();
        info.setCalories(cal);
        info.setProtein(prot);
        info.setCarbohydrates(carb);
        info.setFat(fat);
        info.setFiber(fib);
        info.setSugar(sug);
        return info;
    }

    /**
     * Main method: Analyze meal image using two-stage pipeline
     */
    public MealImage analyzeMeal(byte[] imageBytes, String mimeType) throws IOException {
        if (this.client == null) {
            throw new IllegalStateException("Client not configured");
        }

        log.info("Starting two-stage nutrition analysis...");

        // Stage 1: Identify dishes and portions
        List<DetectedDish> dishes = identifyDishes(imageBytes, mimeType);
        log.info("Stage 1 complete: Detected {} dishes", dishes.size());

        List<FoodItem> detectedFoods = new ArrayList<>();
        double totalCalories = 0.0;
        double totalProtein = 0.0;
        double totalCarbs = 0.0;
        double totalFat = 0.0;
        double totalFiber = 0.0;
        double totalSugar = 0.0;

        // Stage 2 & 3: For each dish, break down and calculate nutrition
        for (DetectedDish dish : dishes) {
            log.info("Processing dish: {} ({}g)", dish.getDishName(), dish.getPortionGrams());

            try {
                // Stage 2: Break down into ingredients
                DishBreakdown breakdown = breakdownDish(dish.getDishName(), dish.getPortionGrams());
                log.info("Stage 2 complete: {} has {} ingredients", dish.getDishName(),
                        breakdown.getIngredients().size());

                // Stage 3: Calculate nutrition from ingredients
                List<IngredientInfo> ingredientInfos = new ArrayList<>();
                NutritionInfo dishNutrition = new NutritionInfo();
                dishNutrition.setCalories(0.0);
                dishNutrition.setProtein(0.0);
                dishNutrition.setCarbohydrates(0.0);
                dishNutrition.setFat(0.0);
                dishNutrition.setFiber(0.0);
                dishNutrition.setSugar(0.0);

                for (DishBreakdown.Ingredient ingredient : breakdown.getIngredients()) {
                    NutritionInfo ingredientNutrition = calculateIngredientNutrition(
                            ingredient.getName(),
                            ingredient.getQuantityGrams());

                    // Create ingredient info
                    IngredientInfo ingredientInfo = new IngredientInfo();
                    ingredientInfo.setName(ingredient.getName());
                    ingredientInfo.setQuantityGrams(ingredient.getQuantityGrams());
                    ingredientInfo.setCategory(ingredient.getCategory());
                    ingredientInfo.setNutrition(ingredientNutrition);
                    ingredientInfos.add(ingredientInfo);

                    // Aggregate to dish nutrition
                    dishNutrition.setCalories(dishNutrition.getCalories() + ingredientNutrition.getCalories());
                    dishNutrition.setProtein(dishNutrition.getProtein() + ingredientNutrition.getProtein());
                    dishNutrition.setCarbohydrates(
                            dishNutrition.getCarbohydrates() + ingredientNutrition.getCarbohydrates());
                    dishNutrition.setFat(dishNutrition.getFat() + ingredientNutrition.getFat());
                    dishNutrition.setFiber(dishNutrition.getFiber() + ingredientNutrition.getFiber());
                    dishNutrition.setSugar(dishNutrition.getSugar() + ingredientNutrition.getSugar());
                }

                // Create food item
                FoodItem foodItem = new FoodItem();
                foodItem.setName(dish.getDishName());
                foodItem.setQuantity(dish.getPortionGrams());
                foodItem.setConfidence(dish.getConfidence());
                foodItem.setVisualCues(dish.getVisualCues());
                foodItem.setCategory(dish.getCategory());
                foodItem.setNutrition(dishNutrition);
                foodItem.setIngredientBreakdown(ingredientInfos);
                detectedFoods.add(foodItem);

                // Aggregate to total
                totalCalories += dishNutrition.getCalories();
                totalProtein += dishNutrition.getProtein();
                totalCarbs += dishNutrition.getCarbohydrates();
                totalFat += dishNutrition.getFat();
                totalFiber += dishNutrition.getFiber();
                totalSugar += dishNutrition.getSugar();

                log.info("Stage 3 complete: {} nutrition calculated", dish.getDishName());

            } catch (Exception e) {
                log.error("Error processing dish: {}", dish.getDishName(), e);
                // Continue with other dishes
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
        summary.setTotalSugar(formatDouble(totalSugar));
        result.setNutritionSummary(summary);

        log.info("Analysis complete. Total calories: {}", summary.getTotalCalories());
        return result;
    }

    /**
     * Stage 1: Identify dishes and estimate portions using Gemini Vision
     */
    private List<DetectedDish> identifyDishes(byte[] imageBytes, String mimeType) throws IOException {
        String prompt = buildStage1Prompt();

        // Create parts list
        java.util.List<Part> parts = new java.util.ArrayList<>();
        parts.add(Part.fromText(prompt));

        // Build an inline blob with bytes + MIME
        Blob inputBlob = Blob.builder()
                .mimeType(mimeType)
                .data(imageBytes)
                .build();

        // Add image part using builder
        Part imagePart = Part.builder()
                .inlineData(inputBlob)
                .build();
        parts.add(imagePart);

        // Create content
        Content content = Content.fromParts(parts.toArray(new Part[0]));

        // Configure generation
        GenerateContentConfig config = GenerateContentConfig.builder()
                .topK(32f)
                .topP(1.0f)
                .maxOutputTokens(2048)
                .build();

        // Generate content using the correct API
        GenerateContentResponse response = client.models.generateContent(modelName, content, config);

        // Extract text from response using quick accessor
        String responseText = response.text();

        // Clean up response
        responseText = responseText.replaceAll("```json", "").replaceAll("```", "").trim();
        log.debug("Stage 1 response: {}", responseText);

        // Parse JSON
        List<DetectedDish> dishes = objectMapper.readValue(responseText, new TypeReference<List<DetectedDish>>() {
        });

        return dishes;
    }

    /**
     * Stage 2: Break down dish into ingredients using Gemini Text
     */
    private DishBreakdown breakdownDish(String dishName, Integer portionGrams) throws IOException {
        String prompt = buildStage2Prompt(dishName, portionGrams);

        // Create text-only content using SDK helper method
        Content content = Content.fromParts(Part.fromText(prompt));

        // Configure generation
        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.3f)
                .topK(32f)
                .topP(1.0f)
                .maxOutputTokens(1024)
                .build();

        // Generate content using the correct API
        GenerateContentResponse response = client.models.generateContent(modelName, content, config);

        // Extract text from response using quick accessor
        String responseText = response.text();

        // Clean up response
        responseText = responseText.replaceAll("```json", "").replaceAll("```", "").trim();
        log.debug("Stage 2 response for {}: {}", dishName, responseText);

        // Parse JSON
        DishBreakdown breakdown = objectMapper.readValue(responseText, DishBreakdown.class);

        return breakdown;
    }

    /**
     * Stage 3: Calculate nutrition for an ingredient
     */
    private NutritionInfo calculateIngredientNutrition(String ingredientName, Integer quantityGrams) {
        // Look up in database
        FoodComposition food = nutritionDatabaseService.findFoodByName(ingredientName);

        if (food != null) {
            double factor = quantityGrams / 100.0;
            NutritionInfo info = new NutritionInfo();
            info.setCalories(formatDouble(food.getEnergyKcal() * factor));
            info.setProtein(formatDouble(food.getProtein() * factor));
            info.setCarbohydrates(formatDouble(food.getCarbohydrate() * factor));
            info.setFat(formatDouble(food.getTotalFat() * factor));
            info.setFiber(formatDouble(food.getTotalFiber() * factor));
            info.setSugar(0.0);
            return info;
        }

        // Try generic nutrition
        for (Map.Entry<String, NutritionInfo> entry : GENERIC_NUTRITION.entrySet()) {
            if (ingredientName.toLowerCase().contains(entry.getKey())) {
                NutritionInfo generic = entry.getValue();
                double factor = quantityGrams / 100.0;

                NutritionInfo info = new NutritionInfo();
                info.setCalories(formatDouble(generic.getCalories() * factor));
                info.setProtein(formatDouble(generic.getProtein() * factor));
                info.setCarbohydrates(formatDouble(generic.getCarbohydrates() * factor));
                info.setFat(formatDouble(generic.getFat() * factor));
                info.setFiber(formatDouble(generic.getFiber() * factor));
                info.setSugar(formatDouble(generic.getSugar() * factor));
                return info;
            }
        }

        // Fallback: return zeros
        log.warn("No nutrition data found for ingredient: {}", ingredientName);
        return createNutritionInfo(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    private String buildStage1Prompt() {
        return """
                You are an expert Indian food nutritionist analyzing a meal image.

                TASK:
                1. Identify ALL distinct dishes/food items visible in the image
                2. For EACH item, provide:
                   - Specific dish name (e.g., "Dal Makhani" not just "Dal")
                   - Estimated portion size in grams
                   - Confidence score (0.0 to 1.0)
                   - Visual reasoning for portion estimate

                PORTION ESTIMATION GUIDELINES:
                - Use plate/bowl size as reference (standard plate ~25cm diameter)
                - Use utensils for scale (spoon ~15cm)
                - Consider depth/height of food
                - Common serving sizes:
                  * Rice/Biryani: 150-250g (1-1.5 cups)
                  * Dal/Curry: 150-200g (1 bowl)
                  * Roti/Naan: 40-60g each
                  * Sabzi: 100-150g
                  * Raita: 50-100g
                  * Desserts: 50-100g

                DISH SPECIFICITY:
                - Don't say "Dal" → Say "Dal Makhani" or "Dal Tadka" or "Sambar"
                - Don't say "Rice" → Say "Jeera Rice" or "Plain Rice" or "Biryani"
                - Don't say "Curry" → Say "Paneer Butter Masala" or "Chicken Curry"
                - Don't say "Bread" → Say "Roti" or "Naan" or "Paratha"

                OUTPUT FORMAT (JSON only, no markdown):
                [
                  {
                    "dishName": "Dal Makhani",
                    "portionGrams": 200,
                    "confidence": 0.92,
                    "visualCues": "Medium-sized bowl, approximately 1 cup, dark brown color with cream",
                    "category": "main_course"
                  }
                ]

                IMPORTANT:
                - Be as specific as possible with dish names
                - Use visual cues (color, texture, garnish) to identify exact dish
                - If unsure between similar dishes, choose most common variant
                - Only include items you can clearly see
                - Minimum confidence threshold: 0.7
                """;
    }

    private String buildStage2Prompt(String dishName, Integer portionGrams) {
        return String.format("""
                You are an expert Indian chef and nutritionist.

                TASK: Break down the following dish into its base ingredients with estimated quantities.

                DISH: %s
                TOTAL PORTION: %dg

                REQUIREMENTS:
                1. List ALL major ingredients (>5%% of total weight)
                2. Provide quantity in grams for each ingredient
                3. Quantities should sum to approximately the total portion
                4. Use standard recipe proportions for Indian cuisine
                5. Include cooking medium (oil/ghee/butter)

                INGREDIENT CATEGORIES TO INCLUDE:
                - Main ingredient (lentils, rice, vegetables, meat)
                - Cooking medium (oil, ghee, butter)
                - Dairy (cream, yogurt, paneer)
                - Vegetables/aromatics (onion, tomato, garlic, ginger)
                - Spices (combined weight)

                OUTPUT FORMAT (JSON only, no markdown):
                {
                  "dishName": "%s",
                  "totalPortionGrams": %d,
                  "ingredients": [
                    {
                      "name": "Black lentils",
                      "quantityGrams": 80,
                      "category": "protein"
                    },
                    {
                      "name": "Butter",
                      "quantityGrams": 15,
                      "category": "fat"
                    }
                  ],
                  "cookingMethod": "slow_cooked",
                  "confidence": 0.90
                }

                Be precise and realistic with quantities. Ingredients should sum to approximately %dg.
                """, dishName, portionGrams, dishName, portionGrams, portionGrams);
    }

    private Double formatDouble(Double val) {
        if (val == null)
            return 0.0;
        return Math.round(val * 100.0) / 100.0;
    }

    /**
     * Generate diet recommendation based on recent stats
     */
    public DietRecommendationResponse generateDietRecommendation(
            List<MonthlyStatsResponse.DailyBreakdown> recentStats,
            UserProfileResponse userProfile) throws IOException {

        String prompt = buildRecommendationPrompt(recentStats, userProfile);

        // Create text-only content
        Content content = Content.fromParts(Part.fromText(prompt));

        // Configure generation
        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.7f) // Slightly higher for creativity
                .topK(32f)
                .topP(1.0f)
                .maxOutputTokens(1024)
                .build();

        // Generate content
        GenerateContentResponse response = client.models.generateContent(modelName, content, config);

        // Extract text
        String responseText = response.text();

        // Clean up response
        responseText = responseText.replaceAll("```json", "").replaceAll("```", "").trim();
        log.debug("Recommendation response: {}", responseText);

        // Parse JSON
        return objectMapper.readValue(responseText, DietRecommendationResponse.class);
    }

    private String buildRecommendationPrompt(
            List<MonthlyStatsResponse.DailyBreakdown> recentStats,
            UserProfileResponse userProfile) {

        StringBuilder statsBuilder = new StringBuilder();
        if (recentStats.isEmpty()) {
            statsBuilder.append("No meals logged in the last 7 days.");
        } else {
            for (MonthlyStatsResponse.DailyBreakdown day : recentStats) {
                statsBuilder.append(String.format(
                        "- %s: %s calories, %s protein, %s carbs, %s fat (Met Goals: %s)\n",
                        day.getDate(),
                        day.getCalories(),
                        day.getProtein(),
                        day.getCarbohydrates(),
                        day.getFat(),
                        day.getMetGoals() ? "Yes" : "No"));
            }
        }

        String goalsStr = "Standard 2000 calorie diet";
        if (userProfile.getNutritionGoals() != null) {
            UserProfileResponse.NutritionGoals g = userProfile.getNutritionGoals();
            goalsStr = String.format(
                    "Calories: %.0f, Protein: %.0fg, Carbs: %.0fg, Fat: %.0fg",
                    g.getCalories(), g.getProtein(), g.getCarbohydrates(), g.getFat());
        }

        return String.format("""
                You are an expert personalized nutritionist.

                Analyze this user's recent nutrition data against their goals and provide actionable recommendations.

                USER GOALS:
                %s

                RECENT ACTIVITY (Last 7 days):
                %s

                TASK:
                1. Suggest 3 specific, actionable diet changes to improve their nutrition.
                2. Suggest 3 specific healthy food add-ons they should eat more of.
                3. Write a short, encouraging summary recommendation.

                OUTPUT FORMAT (JSON only, no markdown):
                {
                  "recommendation": "Your overall summary and encouragement here...",
                  "suggestedChanges": [
                    "Reduce dinner portion sizes...",
                    "Swap white rice for brown rice...",
                    "Increase protein at breakfast..."
                  ],
                  "suggestedAddOns": [
                    "Greek yogurt for snacks",
                    "Spinach in smoothies",
                    "Almonds"
                  ]
                }
                """, goalsStr, statsBuilder.toString());
    }
}