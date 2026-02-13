package com.project.NutriTracker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyStatsResponse {

    private Integer year;
    private Integer totalMeals;
    private Integer totalDaysLogged;
    private Integer daysInYear;

    // Average daily nutrition for the year
    private AverageNutrition averageDaily;

    // Total yearly nutrition
    private TotalNutrition totalYearly;

    // Goals and adherence
    private NutritionGoals yearlyGoals;
    private GoalAdherence goalAdherence;

    // Monthly breakdown
    private List<MonthlyBreakdown> monthlyBreakdown;

    // Trends and insights
    private YearlyTrends trends;

    // Best and worst months
    private MonthSummary bestMonth;
    private MonthSummary worstMonth;

    // Seasonal patterns (optional)
    private SeasonalPatterns seasonalPatterns;

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
        private Double caloriesAdherenceRate;
        private Double proteinAdherenceRate;
        private Double carbsAdherenceRate;
        private Double fatAdherenceRate;
        private Double overallAdherenceRate;
        private Integer monthsMetGoals; // Number of months with >70% adherence
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyBreakdown {
        private String month; // "2026-01", "2026-02", etc.
        private Integer mealsCount;
        private Integer daysLogged;
        private Double averageCalories;
        private Double averageProtein;
        private Double averageCarbs;
        private Double averageFat;
        private Double adherenceRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyTrends {
        private String overallTrend; // IMPROVING, DECLINING, STABLE
        private String caloriesTrend;
        private String proteinTrend;
        private String carbsTrend;
        private String fatTrend;
        private Double averageCalorieDeficit;
        private MacroDistribution macroDistribution;
        private String mostConsistentMonth; // Month with best adherence
        private String leastConsistentMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MacroDistribution {
        private Double proteinPercentage;
        private Double carbsPercentage;
        private Double fatPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        private String month;
        private Double averageCalories;
        private Integer mealsLogged;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeasonalPatterns {
        private SeasonStats winter; // Dec, Jan, Feb
        private SeasonStats spring; // Mar, Apr, May
        private SeasonStats summer; // Jun, Jul, Aug
        private SeasonStats autumn; // Sep, Oct, Nov
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeasonStats {
        private Double averageCalories;
        private Double averageProtein;
        private Double averageCarbs;
        private Double averageFat;
        private Integer totalMeals;
    }
}
