package com.project.NutriTracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.NutriTracker.document.MealImage;
import com.project.NutriTracker.document.User;
import com.project.NutriTracker.dto.DailyStatsResponse;
import com.project.NutriTracker.dto.UserProfileResponse;
import com.project.NutriTracker.exception.ResourceNotFoundException;
import com.project.NutriTracker.repository.MealImageRepository;
import com.project.NutriTracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final MealImageRepository mealImageRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Get daily nutrition stats for a specific date
     */
    public DailyStatsResponse getDailyStats(String email, LocalDate date) {
        // Get user profile to fetch nutrition goals
        UserProfileResponse userProfile = userService.getUserProfile(email);

        // Get all meals for the specified date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<MealImage> meals = mealImageRepository.findByUserIdAndUploadedAtBetween(
                email, startOfDay, endOfDay);

        // Filter only analyzed meals
        List<MealImage> analyzedMeals = meals.stream()
                .filter(meal -> "ANALYZED".equals(meal.getStatus()))
                .collect(Collectors.toList());

        // Calculate total consumed nutrition
        DailyStatsResponse.NutritionConsumed consumed = calculateTotalNutrition(analyzedMeals);

        // Get nutrition goals from user profile
        DailyStatsResponse.NutritionGoals goals = buildNutritionGoals(userProfile);

        // Calculate remaining nutrition
        DailyStatsResponse.NutritionRemaining remaining = calculateRemaining(consumed, goals);

        // Build meal summaries
        List<DailyStatsResponse.MealSummary> mealSummaries = buildMealSummaries(analyzedMeals);

        return DailyStatsResponse.builder()
                .date(date)
                .consumed(consumed)
                .goals(goals)
                .remaining(remaining)
                .meals(mealSummaries)
                .totalMeals(analyzedMeals.size())
                .build();
    }

    /**
     * Calculate total nutrition from all meals
     */
    private DailyStatsResponse.NutritionConsumed calculateTotalNutrition(List<MealImage> meals) {
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        double totalFiber = 0;
        double totalSugar = 0;

        for (MealImage meal : meals) {
            if (meal.getNutritionSummary() != null) {
                MealImage.NutritionSummary summary = meal.getNutritionSummary();
                totalCalories += summary.getTotalCalories() != null ? summary.getTotalCalories() : 0;
                totalProtein += summary.getTotalProtein() != null ? summary.getTotalProtein() : 0;
                totalCarbs += summary.getTotalCarbohydrates() != null ? summary.getTotalCarbohydrates() : 0;
                totalFat += summary.getTotalFat() != null ? summary.getTotalFat() : 0;
                totalFiber += summary.getTotalFiber() != null ? summary.getTotalFiber() : 0;
                totalSugar += summary.getTotalSugar() != null ? summary.getTotalSugar() : 0;
            }
        }

        return DailyStatsResponse.NutritionConsumed.builder()
                .calories(Math.round(totalCalories * 100.0) / 100.0)
                .protein(Math.round(totalProtein * 100.0) / 100.0)
                .carbohydrates(Math.round(totalCarbs * 100.0) / 100.0)
                .fat(Math.round(totalFat * 100.0) / 100.0)
                .fiber(Math.round(totalFiber * 100.0) / 100.0)
                .sugar(Math.round(totalSugar * 100.0) / 100.0)
                .build();
    }

    /**
     * Build nutrition goals from user profile
     */
    private DailyStatsResponse.NutritionGoals buildNutritionGoals(UserProfileResponse userProfile) {
        if (userProfile.getNutritionGoals() == null) {
            return DailyStatsResponse.NutritionGoals.builder()
                    .calories(2000.0)
                    .protein(150.0)
                    .carbohydrates(200.0)
                    .fat(67.0)
                    .build();
        }

        UserProfileResponse.NutritionGoals profileGoals = userProfile.getNutritionGoals();
        return DailyStatsResponse.NutritionGoals.builder()
                .calories(profileGoals.getCalories())
                .protein(profileGoals.getProtein())
                .carbohydrates(profileGoals.getCarbohydrates())
                .fat(profileGoals.getFat())
                .build();
    }

    /**
     * Calculate remaining nutrition (goals - consumed)
     */
    private DailyStatsResponse.NutritionRemaining calculateRemaining(
            DailyStatsResponse.NutritionConsumed consumed,
            DailyStatsResponse.NutritionGoals goals) {

        return DailyStatsResponse.NutritionRemaining.builder()
                .calories(Math.round((goals.getCalories() - consumed.getCalories()) * 100.0) / 100.0)
                .protein(Math.round((goals.getProtein() - consumed.getProtein()) * 100.0) / 100.0)
                .carbohydrates(Math.round((goals.getCarbohydrates() - consumed.getCarbohydrates()) * 100.0) / 100.0)
                .fat(Math.round((goals.getFat() - consumed.getFat()) * 100.0) / 100.0)
                .build();
    }

    /**
     * Build meal summaries
     */
    private List<DailyStatsResponse.MealSummary> buildMealSummaries(List<MealImage> meals) {
        return meals.stream()
                .map(meal -> {
                    MealImage.NutritionSummary summary = meal.getNutritionSummary();
                    DailyStatsResponse.NutritionConsumed nutrition = DailyStatsResponse.NutritionConsumed.builder()
                            .calories(summary != null ? summary.getTotalCalories() : 0.0)
                            .protein(summary != null ? summary.getTotalProtein() : 0.0)
                            .carbohydrates(summary != null ? summary.getTotalCarbohydrates() : 0.0)
                            .fat(summary != null ? summary.getTotalFat() : 0.0)
                            .fiber(summary != null ? summary.getTotalFiber() : 0.0)
                            .sugar(summary != null ? summary.getTotalSugar() : 0.0)
                            .build();

                    List<String> foodItems = meal.getDetectedFoods() != null
                            ? meal.getDetectedFoods().stream()
                                    .map(MealImage.FoodItem::getName)
                                    .collect(Collectors.toList())
                            : List.of();

                    return DailyStatsResponse.MealSummary.builder()
                            .id(meal.getId())
                            .imageUrl(meal.getImageUrl())
                            .uploadedAt(meal.getUploadedAt().toString())
                            .nutrition(nutrition)
                            .foodItems(foodItems)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
