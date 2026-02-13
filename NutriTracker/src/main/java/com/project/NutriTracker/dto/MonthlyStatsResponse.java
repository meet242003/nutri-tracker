package com.project.NutriTracker.dto;

import java.time.YearMonth;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatsResponse {

    private YearMonth month;
    private Integer totalMeals;
    private Integer totalDaysLogged;
    private Integer daysInMonth;

    // Average daily nutrition
    private AverageNutrition averageDaily;

    // Total monthly nutrition
    private TotalNutrition totalMonthly;

    // Goals and adherence
    private NutritionGoals monthlyGoals;
    private GoalAdherence goalAdherence;

    // Trends and insights
    private NutritionTrends trends;

    // Best and worst days
    private DaySummary bestDay;
    private DaySummary worstDay;

    // Daily breakdown (optional, for charts)
    private List<DailyBreakdown> dailyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AverageNutrition {
        private Double calories;
        private Double protein;
        private Double carbohydrates;
        private Double fat;
        private Double fiber;
        private Double sugar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalNutrition {
        private Double calories;
        private Double protein;
        private Double carbohydrates;
        private Double fat;
        private Double fiber;
        private Double sugar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionGoals {
        private Double dailyCalories;
        private Double dailyProtein;
        private Double dailyCarbohydrates;
        private Double dailyFat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalAdherence {
        private Double caloriesAdherenceRate; // % of days meeting calorie goal (within ±10%)
        private Double proteinAdherenceRate;
        private Double carbsAdherenceRate;
        private Double fatAdherenceRate;
        private Double overallAdherenceRate; // Average of all rates
        private Integer daysMetGoals; // Number of days meeting all goals
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionTrends {
        private String caloriesTrend; // INCREASING, DECREASING, STABLE
        private String proteinTrend;
        private String carbsTrend;
        private String fatTrend;
        private Double averageCalorieDeficit; // Negative = surplus
        private MacroDistribution macroDistribution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MacroDistribution {
        private Double proteinPercentage; // % of total calories from protein
        private Double carbsPercentage;
        private Double fatPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaySummary {
        private String date;
        private Double calories;
        private Integer mealsLogged;
        private String reason; // e.g., "Highest calorie day" or "Lowest calorie day"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyBreakdown {
        private String date;
        private Double calories;
        private Double protein;
        private Double carbohydrates;
        private Double fat;
        private Integer mealsCount;
        private Boolean metGoals;
    }
}
