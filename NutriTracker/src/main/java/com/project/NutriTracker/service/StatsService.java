package com.project.NutriTracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.NutriTracker.document.MealImage;
import com.project.NutriTracker.document.User;
import com.project.NutriTracker.dto.DailyStatsResponse;
import com.project.NutriTracker.dto.MonthlyStatsResponse;
import com.project.NutriTracker.dto.UserProfileResponse;
import com.project.NutriTracker.dto.YearlyStatsResponse;
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
                                totalCarbs += summary.getTotalCarbohydrates() != null ? summary.getTotalCarbohydrates()
                                                : 0;
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
                                .carbohydrates(Math
                                                .round((goals.getCarbohydrates() - consumed.getCarbohydrates()) * 100.0)
                                                / 100.0)
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
                                        DailyStatsResponse.NutritionConsumed nutrition = DailyStatsResponse.NutritionConsumed
                                                        .builder()
                                                        .calories(summary != null ? summary.getTotalCalories() : 0.0)
                                                        .protein(summary != null ? summary.getTotalProtein() : 0.0)
                                                        .carbohydrates(summary != null ? summary.getTotalCarbohydrates()
                                                                        : 0.0)
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

        /**
         * Get monthly nutrition stats
         */
        public MonthlyStatsResponse getMonthlyStats(String email, int year, int month) {
                YearMonth yearMonth = YearMonth.of(year, month);
                LocalDate startDate = yearMonth.atDay(1);
                LocalDate endDate = yearMonth.atEndOfMonth();

                log.info("Fetching monthly stats for user: {} for {}-{}", email, year, month);

                // Get user profile for goals
                UserProfileResponse userProfile = userService.getUserProfile(email);
                MonthlyStatsResponse.NutritionGoals goals = buildMonthlyGoals(userProfile);

                // Get all meals for the month
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
                List<MealImage> meals = mealImageRepository.findByUserIdAndUploadedAtBetween(
                                email, startDateTime, endDateTime);

                // Filter analyzed meals
                List<MealImage> analyzedMeals = meals.stream()
                                .filter(meal -> "ANALYZED".equals(meal.getStatus()))
                                .collect(Collectors.toList());

                // Group meals by date
                Map<LocalDate, List<MealImage>> mealsByDate = analyzedMeals.stream()
                                .collect(Collectors.groupingBy(meal -> meal.getUploadedAt().toLocalDate()));

                // Calculate daily breakdowns
                List<MonthlyStatsResponse.DailyBreakdown> dailyBreakdowns = new ArrayList<>();
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                        List<MealImage> dayMeals = mealsByDate.getOrDefault(date, List.of());
                        if (!dayMeals.isEmpty()) {
                                dailyBreakdowns.add(buildDailyBreakdown(date, dayMeals, goals));
                        }
                }

                // Calculate aggregated stats
                int totalMeals = analyzedMeals.size();
                int totalDaysLogged = mealsByDate.size();
                int daysInMonth = yearMonth.lengthOfMonth();

                MonthlyStatsResponse.AverageNutrition avgNutrition = calculateAverageNutrition(dailyBreakdowns);
                MonthlyStatsResponse.TotalNutrition totalNutrition = calculateTotalNutrition(analyzedMeals);
                MonthlyStatsResponse.GoalAdherence adherence = calculateGoalAdherence(dailyBreakdowns, goals);
                MonthlyStatsResponse.NutritionTrends trends = calculateMonthlyTrends(dailyBreakdowns, goals);

                // Find best and worst days
                MonthlyStatsResponse.DaySummary bestDay = findBestDay(dailyBreakdowns);
                MonthlyStatsResponse.DaySummary worstDay = findWorstDay(dailyBreakdowns);

                return MonthlyStatsResponse.builder()
                                .month(yearMonth)
                                .totalMeals(totalMeals)
                                .totalDaysLogged(totalDaysLogged)
                                .daysInMonth(daysInMonth)
                                .averageDaily(avgNutrition)
                                .totalMonthly(totalNutrition)
                                .monthlyGoals(goals)
                                .goalAdherence(adherence)
                                .trends(trends)
                                .bestDay(bestDay)
                                .worstDay(worstDay)
                                .dailyBreakdown(dailyBreakdowns)
                                .build();
        }

        /**
         * Get yearly nutrition stats
         */
        public YearlyStatsResponse getYearlyStats(String email, int year) {
                log.info("Fetching yearly stats for user: {} for year {}", email, year);

                // Get user profile for goals
                UserProfileResponse userProfile = userService.getUserProfile(email);
                YearlyStatsResponse.NutritionGoals goals = buildYearlyGoals(userProfile);

                // Get all meals for the year
                LocalDateTime startDateTime = LocalDate.of(year, 1, 1).atStartOfDay();
                LocalDateTime endDateTime = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
                List<MealImage> meals = mealImageRepository.findByUserIdAndUploadedAtBetween(
                                email, startDateTime, endDateTime);

                // Filter analyzed meals
                List<MealImage> analyzedMeals = meals.stream()
                                .filter(meal -> "ANALYZED".equals(meal.getStatus()))
                                .collect(Collectors.toList());

                // Group meals by month
                Map<YearMonth, List<MealImage>> mealsByMonth = analyzedMeals.stream()
                                .collect(Collectors.groupingBy(meal -> YearMonth.from(meal.getUploadedAt())));

                // Calculate monthly breakdowns
                List<YearlyStatsResponse.MonthlyBreakdown> monthlyBreakdowns = new ArrayList<>();
                for (int m = 1; m <= 12; m++) {
                        YearMonth yearMonth = YearMonth.of(year, m);
                        List<MealImage> monthMeals = mealsByMonth.getOrDefault(yearMonth, List.of());
                        if (!monthMeals.isEmpty()) {
                                monthlyBreakdowns.add(buildMonthlyBreakdown(yearMonth, monthMeals, goals));
                        }
                }

                // Calculate aggregated stats
                int totalMeals = analyzedMeals.size();
                Map<LocalDate, List<MealImage>> mealsByDate = analyzedMeals.stream()
                                .collect(Collectors.groupingBy(meal -> meal.getUploadedAt().toLocalDate()));
                int totalDaysLogged = mealsByDate.size();
                int daysInYear = Year.of(year).length();

                YearlyStatsResponse.AverageNutrition avgNutrition = calculateYearlyAverageNutrition(monthlyBreakdowns);
                YearlyStatsResponse.TotalNutrition totalNutrition = calculateYearlyTotalNutrition(analyzedMeals);
                YearlyStatsResponse.GoalAdherence adherence = calculateYearlyGoalAdherence(monthlyBreakdowns);
                YearlyStatsResponse.YearlyTrends trends = calculateYearlyTrends(monthlyBreakdowns, goals);

                // Find best and worst months
                YearlyStatsResponse.MonthSummary bestMonth = findBestMonth(monthlyBreakdowns);
                YearlyStatsResponse.MonthSummary worstMonth = findWorstMonth(monthlyBreakdowns);

                // Calculate seasonal patterns
                YearlyStatsResponse.SeasonalPatterns seasonalPatterns = calculateSeasonalPatterns(mealsByMonth);

                return YearlyStatsResponse.builder()
                                .year(year)
                                .totalMeals(totalMeals)
                                .totalDaysLogged(totalDaysLogged)
                                .daysInYear(daysInYear)
                                .averageDaily(avgNutrition)
                                .totalYearly(totalNutrition)
                                .yearlyGoals(goals)
                                .goalAdherence(adherence)
                                .monthlyBreakdown(monthlyBreakdowns)
                                .trends(trends)
                                .bestMonth(bestMonth)
                                .worstMonth(worstMonth)
                                .seasonalPatterns(seasonalPatterns)
                                .build();
        }

        // ==================== Helper Methods for Monthly Stats ====================

        private MonthlyStatsResponse.NutritionGoals buildMonthlyGoals(UserProfileResponse userProfile) {
                if (userProfile.getNutritionGoals() == null) {
                        return MonthlyStatsResponse.NutritionGoals.builder()
                                        .dailyCalories(2000.0)
                                        .dailyProtein(150.0)
                                        .dailyCarbohydrates(200.0)
                                        .dailyFat(67.0)
                                        .build();
                }

                UserProfileResponse.NutritionGoals profileGoals = userProfile.getNutritionGoals();
                return MonthlyStatsResponse.NutritionGoals.builder()
                                .dailyCalories(profileGoals.getCalories())
                                .dailyProtein(profileGoals.getProtein())
                                .dailyCarbohydrates(profileGoals.getCarbohydrates())
                                .dailyFat(profileGoals.getFat())
                                .build();
        }

        private MonthlyStatsResponse.DailyBreakdown buildDailyBreakdown(
                        LocalDate date, List<MealImage> dayMeals, MonthlyStatsResponse.NutritionGoals goals) {

                double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;

                for (MealImage meal : dayMeals) {
                        if (meal.getNutritionSummary() != null) {
                                MealImage.NutritionSummary summary = meal.getNutritionSummary();
                                totalCalories += summary.getTotalCalories() != null ? summary.getTotalCalories() : 0;
                                totalProtein += summary.getTotalProtein() != null ? summary.getTotalProtein() : 0;
                                totalCarbs += summary.getTotalCarbohydrates() != null ? summary.getTotalCarbohydrates()
                                                : 0;
                                totalFat += summary.getTotalFat() != null ? summary.getTotalFat() : 0;
                        }
                }

                boolean metGoals = isWithinGoalRange(totalCalories, goals.getDailyCalories(), 0.1) &&
                                isWithinGoalRange(totalProtein, goals.getDailyProtein(), 0.1) &&
                                isWithinGoalRange(totalCarbs, goals.getDailyCarbohydrates(), 0.1) &&
                                isWithinGoalRange(totalFat, goals.getDailyFat(), 0.1);

                return MonthlyStatsResponse.DailyBreakdown.builder()
                                .date(date.toString())
                                .calories(round(totalCalories))
                                .protein(round(totalProtein))
                                .carbohydrates(round(totalCarbs))
                                .fat(round(totalFat))
                                .mealsCount(dayMeals.size())
                                .metGoals(metGoals)
                                .build();
        }

        private MonthlyStatsResponse.AverageNutrition calculateAverageNutrition(
                        List<MonthlyStatsResponse.DailyBreakdown> dailyBreakdowns) {

                if (dailyBreakdowns.isEmpty()) {
                        return MonthlyStatsResponse.AverageNutrition.builder()
                                        .calories(0.0).protein(0.0).carbohydrates(0.0)
                                        .fat(0.0).fiber(0.0).sugar(0.0).build();
                }

                double avgCalories = dailyBreakdowns.stream()
                                .mapToDouble(MonthlyStatsResponse.DailyBreakdown::getCalories).average().orElse(0.0);
                double avgProtein = dailyBreakdowns.stream()
                                .mapToDouble(MonthlyStatsResponse.DailyBreakdown::getProtein).average().orElse(0.0);
                double avgCarbs = dailyBreakdowns.stream()
                                .mapToDouble(MonthlyStatsResponse.DailyBreakdown::getCarbohydrates).average()
                                .orElse(0.0);
                double avgFat = dailyBreakdowns.stream()
                                .mapToDouble(MonthlyStatsResponse.DailyBreakdown::getFat).average().orElse(0.0);

                return MonthlyStatsResponse.AverageNutrition.builder()
                                .calories(round(avgCalories))
                                .protein(round(avgProtein))
                                .carbohydrates(round(avgCarbs))
                                .fat(round(avgFat))
                                .fiber(0.0) // Can be calculated if needed
                                .sugar(0.0) // Can be calculated if needed
                                .build();
        }

        private MonthlyStatsResponse.TotalNutrition calculateTotalNutrition(List<MealImage> meals) {
                double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0, totalFiber = 0,
                                totalSugar = 0;

                for (MealImage meal : meals) {
                        if (meal.getNutritionSummary() != null) {
                                MealImage.NutritionSummary summary = meal.getNutritionSummary();
                                totalCalories += summary.getTotalCalories() != null ? summary.getTotalCalories() : 0;
                                totalProtein += summary.getTotalProtein() != null ? summary.getTotalProtein() : 0;
                                totalCarbs += summary.getTotalCarbohydrates() != null ? summary.getTotalCarbohydrates()
                                                : 0;
                                totalFat += summary.getTotalFat() != null ? summary.getTotalFat() : 0;
                                totalFiber += summary.getTotalFiber() != null ? summary.getTotalFiber() : 0;
                                totalSugar += summary.getTotalSugar() != null ? summary.getTotalSugar() : 0;
                        }
                }

                return MonthlyStatsResponse.TotalNutrition.builder()
                                .calories(round(totalCalories))
                                .protein(round(totalProtein))
                                .carbohydrates(round(totalCarbs))
                                .fat(round(totalFat))
                                .fiber(round(totalFiber))
                                .sugar(round(totalSugar))
                                .build();
        }

        private MonthlyStatsResponse.GoalAdherence calculateGoalAdherence(
                        List<MonthlyStatsResponse.DailyBreakdown> dailyBreakdowns,
                        MonthlyStatsResponse.NutritionGoals goals) {

                if (dailyBreakdowns.isEmpty()) {
                        return MonthlyStatsResponse.GoalAdherence.builder()
                                        .caloriesAdherenceRate(0.0).proteinAdherenceRate(0.0)
                                        .carbsAdherenceRate(0.0).fatAdherenceRate(0.0)
                                        .overallAdherenceRate(0.0).daysMetGoals(0).build();
                }

                long caloriesDaysMetGoal = dailyBreakdowns.stream()
                                .filter(day -> isWithinGoalRange(day.getCalories(), goals.getDailyCalories(), 0.1))
                                .count();
                long proteinDaysMetGoal = dailyBreakdowns.stream()
                                .filter(day -> isWithinGoalRange(day.getProtein(), goals.getDailyProtein(), 0.1))
                                .count();
                long carbsDaysMetGoal = dailyBreakdowns.stream()
                                .filter(day -> isWithinGoalRange(day.getCarbohydrates(), goals.getDailyCarbohydrates(),
                                                0.1))
                                .count();
                long fatDaysMetGoal = dailyBreakdowns.stream()
                                .filter(day -> isWithinGoalRange(day.getFat(), goals.getDailyFat(), 0.1))
                                .count();

                int totalDays = dailyBreakdowns.size();
                double caloriesRate = (caloriesDaysMetGoal * 100.0) / totalDays;
                double proteinRate = (proteinDaysMetGoal * 100.0) / totalDays;
                double carbsRate = (carbsDaysMetGoal * 100.0) / totalDays;
                double fatRate = (fatDaysMetGoal * 100.0) / totalDays;
                double overallRate = (caloriesRate + proteinRate + carbsRate + fatRate) / 4.0;

                int daysMetAllGoals = (int) dailyBreakdowns.stream()
                                .filter(MonthlyStatsResponse.DailyBreakdown::getMetGoals)
                                .count();

                return MonthlyStatsResponse.GoalAdherence.builder()
                                .caloriesAdherenceRate(round(caloriesRate))
                                .proteinAdherenceRate(round(proteinRate))
                                .carbsAdherenceRate(round(carbsRate))
                                .fatAdherenceRate(round(fatRate))
                                .overallAdherenceRate(round(overallRate))
                                .daysMetGoals(daysMetAllGoals)
                                .build();
        }

        private MonthlyStatsResponse.NutritionTrends calculateMonthlyTrends(
                        List<MonthlyStatsResponse.DailyBreakdown> dailyBreakdowns,
                        MonthlyStatsResponse.NutritionGoals goals) {

                if (dailyBreakdowns.size() < 2) {
                        return MonthlyStatsResponse.NutritionTrends.builder()
                                        .caloriesTrend("STABLE")
                                        .proteinTrend("STABLE")
                                        .carbsTrend("STABLE")
                                        .fatTrend("STABLE")
                                        .averageCalorieDeficit(0.0)
                                        .macroDistribution(MonthlyStatsResponse.MacroDistribution.builder()
                                                        .proteinPercentage(0.0).carbsPercentage(0.0).fatPercentage(0.0)
                                                        .build())
                                        .build();
                }

                // Calculate trends (simple linear trend)
                String caloriesTrend = calculateTrend(dailyBreakdowns,
                                MonthlyStatsResponse.DailyBreakdown::getCalories);
                String proteinTrend = calculateTrend(dailyBreakdowns, MonthlyStatsResponse.DailyBreakdown::getProtein);
                String carbsTrend = calculateTrend(dailyBreakdowns,
                                MonthlyStatsResponse.DailyBreakdown::getCarbohydrates);
                String fatTrend = calculateTrend(dailyBreakdowns, MonthlyStatsResponse.DailyBreakdown::getFat);

                // Calculate average calorie deficit
                double avgCalories = dailyBreakdowns.stream()
                                .mapToDouble(MonthlyStatsResponse.DailyBreakdown::getCalories).average().orElse(0.0);
                double avgDeficit = goals.getDailyCalories() - avgCalories;

                // Calculate macro distribution
                double avgProtein = dailyBreakdowns.stream()
                                .mapToDouble(MonthlyStatsResponse.DailyBreakdown::getProtein).average().orElse(0.0);
                double avgCarbs = dailyBreakdowns.stream()
                                .mapToDouble(MonthlyStatsResponse.DailyBreakdown::getCarbohydrates).average()
                                .orElse(0.0);
                double avgFat = dailyBreakdowns.stream()
                                .mapToDouble(MonthlyStatsResponse.DailyBreakdown::getFat).average().orElse(0.0);

                double proteinCals = avgProtein * 4;
                double carbsCals = avgCarbs * 4;
                double fatCals = avgFat * 9;
                double totalCals = proteinCals + carbsCals + fatCals;

                MonthlyStatsResponse.MacroDistribution macroDistribution = MonthlyStatsResponse.MacroDistribution
                                .builder()
                                .proteinPercentage(totalCals > 0 ? round((proteinCals / totalCals) * 100) : 0.0)
                                .carbsPercentage(totalCals > 0 ? round((carbsCals / totalCals) * 100) : 0.0)
                                .fatPercentage(totalCals > 0 ? round((fatCals / totalCals) * 100) : 0.0)
                                .build();

                return MonthlyStatsResponse.NutritionTrends.builder()
                                .caloriesTrend(caloriesTrend)
                                .proteinTrend(proteinTrend)
                                .carbsTrend(carbsTrend)
                                .fatTrend(fatTrend)
                                .averageCalorieDeficit(round(avgDeficit))
                                .macroDistribution(macroDistribution)
                                .build();
        }

        private MonthlyStatsResponse.DaySummary findBestDay(List<MonthlyStatsResponse.DailyBreakdown> dailyBreakdowns) {
                if (dailyBreakdowns.isEmpty()) {
                        return null;
                }

                MonthlyStatsResponse.DailyBreakdown best = dailyBreakdowns.stream()
                                .filter(MonthlyStatsResponse.DailyBreakdown::getMetGoals)
                                .max(Comparator.comparingInt(MonthlyStatsResponse.DailyBreakdown::getMealsCount))
                                .orElse(dailyBreakdowns.get(0));

                return MonthlyStatsResponse.DaySummary.builder()
                                .date(best.getDate())
                                .calories(best.getCalories())
                                .mealsLogged(best.getMealsCount())
                                .reason(best.getMetGoals() ? "Met all nutrition goals" : "Most meals logged")
                                .build();
        }

        private MonthlyStatsResponse.DaySummary findWorstDay(
                        List<MonthlyStatsResponse.DailyBreakdown> dailyBreakdowns) {
                if (dailyBreakdowns.isEmpty()) {
                        return null;
                }

                MonthlyStatsResponse.DailyBreakdown worst = dailyBreakdowns.stream()
                                .min(Comparator.comparingInt(MonthlyStatsResponse.DailyBreakdown::getMealsCount))
                                .orElse(dailyBreakdowns.get(dailyBreakdowns.size() - 1));

                return MonthlyStatsResponse.DaySummary.builder()
                                .date(worst.getDate())
                                .calories(worst.getCalories())
                                .mealsLogged(worst.getMealsCount())
                                .reason("Fewest meals logged")
                                .build();
        }

        // ==================== Helper Methods for Yearly Stats ====================

        private YearlyStatsResponse.NutritionGoals buildYearlyGoals(UserProfileResponse userProfile) {
                if (userProfile.getNutritionGoals() == null) {
                        return YearlyStatsResponse.NutritionGoals.builder()
                                        .dailyCalories(2000.0)
                                        .dailyProtein(150.0)
                                        .dailyCarbohydrates(200.0)
                                        .dailyFat(67.0)
                                        .build();
                }

                UserProfileResponse.NutritionGoals profileGoals = userProfile.getNutritionGoals();
                return YearlyStatsResponse.NutritionGoals.builder()
                                .dailyCalories(profileGoals.getCalories())
                                .dailyProtein(profileGoals.getProtein())
                                .dailyCarbohydrates(profileGoals.getCarbohydrates())
                                .dailyFat(profileGoals.getFat())
                                .build();
        }

        private YearlyStatsResponse.MonthlyBreakdown buildMonthlyBreakdown(
                        YearMonth yearMonth, List<MealImage> monthMeals, YearlyStatsResponse.NutritionGoals goals) {

                Map<LocalDate, List<MealImage>> mealsByDate = monthMeals.stream()
                                .collect(Collectors.groupingBy(meal -> meal.getUploadedAt().toLocalDate()));

                int daysLogged = mealsByDate.size();

                double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
                for (MealImage meal : monthMeals) {
                        if (meal.getNutritionSummary() != null) {
                                MealImage.NutritionSummary summary = meal.getNutritionSummary();
                                totalCalories += summary.getTotalCalories() != null ? summary.getTotalCalories() : 0;
                                totalProtein += summary.getTotalProtein() != null ? summary.getTotalProtein() : 0;
                                totalCarbs += summary.getTotalCarbohydrates() != null ? summary.getTotalCarbohydrates()
                                                : 0;
                                totalFat += summary.getTotalFat() != null ? summary.getTotalFat() : 0;
                        }
                }

                double avgCalories = daysLogged > 0 ? totalCalories / daysLogged : 0;
                double avgProtein = daysLogged > 0 ? totalProtein / daysLogged : 0;
                double avgCarbs = daysLogged > 0 ? totalCarbs / daysLogged : 0;
                double avgFat = daysLogged > 0 ? totalFat / daysLogged : 0;

                // Calculate adherence for this month
                long daysMetGoals = mealsByDate.entrySet().stream()
                                .filter(entry -> {
                                        double dayCalories = entry.getValue().stream()
                                                        .mapToDouble(m -> m.getNutritionSummary() != null
                                                                        ? m.getNutritionSummary().getTotalCalories()
                                                                        : 0)
                                                        .sum();
                                        return isWithinGoalRange(dayCalories, goals.getDailyCalories(), 0.1);
                                })
                                .count();

                double adherenceRate = daysLogged > 0 ? (daysMetGoals * 100.0) / daysLogged : 0.0;

                return YearlyStatsResponse.MonthlyBreakdown.builder()
                                .month(yearMonth.toString())
                                .mealsCount(monthMeals.size())
                                .daysLogged(daysLogged)
                                .averageCalories(round(avgCalories))
                                .averageProtein(round(avgProtein))
                                .averageCarbs(round(avgCarbs))
                                .averageFat(round(avgFat))
                                .adherenceRate(round(adherenceRate))
                                .build();
        }

        private YearlyStatsResponse.AverageNutrition calculateYearlyAverageNutrition(
                        List<YearlyStatsResponse.MonthlyBreakdown> monthlyBreakdowns) {

                if (monthlyBreakdowns.isEmpty()) {
                        return YearlyStatsResponse.AverageNutrition.builder()
                                        .calories(0.0).protein(0.0).carbohydrates(0.0)
                                        .fat(0.0).fiber(0.0).sugar(0.0).build();
                }

                double avgCalories = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAverageCalories).average()
                                .orElse(0.0);
                double avgProtein = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAverageProtein).average()
                                .orElse(0.0);
                double avgCarbs = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAverageCarbs).average()
                                .orElse(0.0);
                double avgFat = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAverageFat).average().orElse(0.0);

                return YearlyStatsResponse.AverageNutrition.builder()
                                .calories(round(avgCalories))
                                .protein(round(avgProtein))
                                .carbohydrates(round(avgCarbs))
                                .fat(round(avgFat))
                                .fiber(0.0)
                                .sugar(0.0)
                                .build();
        }

        private YearlyStatsResponse.TotalNutrition calculateYearlyTotalNutrition(List<MealImage> meals) {
                double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0, totalFiber = 0,
                                totalSugar = 0;

                for (MealImage meal : meals) {
                        if (meal.getNutritionSummary() != null) {
                                MealImage.NutritionSummary summary = meal.getNutritionSummary();
                                totalCalories += summary.getTotalCalories() != null ? summary.getTotalCalories() : 0;
                                totalProtein += summary.getTotalProtein() != null ? summary.getTotalProtein() : 0;
                                totalCarbs += summary.getTotalCarbohydrates() != null ? summary.getTotalCarbohydrates()
                                                : 0;
                                totalFat += summary.getTotalFat() != null ? summary.getTotalFat() : 0;
                                totalFiber += summary.getTotalFiber() != null ? summary.getTotalFiber() : 0;
                                totalSugar += summary.getTotalSugar() != null ? summary.getTotalSugar() : 0;
                        }
                }

                return YearlyStatsResponse.TotalNutrition.builder()
                                .calories(round(totalCalories))
                                .protein(round(totalProtein))
                                .carbohydrates(round(totalCarbs))
                                .fat(round(totalFat))
                                .fiber(round(totalFiber))
                                .sugar(round(totalSugar))
                                .build();
        }

        private YearlyStatsResponse.GoalAdherence calculateYearlyGoalAdherence(
                        List<YearlyStatsResponse.MonthlyBreakdown> monthlyBreakdowns) {

                if (monthlyBreakdowns.isEmpty()) {
                        return YearlyStatsResponse.GoalAdherence.builder()
                                        .caloriesAdherenceRate(0.0).proteinAdherenceRate(0.0)
                                        .carbsAdherenceRate(0.0).fatAdherenceRate(0.0)
                                        .overallAdherenceRate(0.0).monthsMetGoals(0).build();
                }

                double avgAdherence = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAdherenceRate)
                                .average().orElse(0.0);

                int monthsMetGoals = (int) monthlyBreakdowns.stream()
                                .filter(month -> month.getAdherenceRate() >= 70.0)
                                .count();

                return YearlyStatsResponse.GoalAdherence.builder()
                                .caloriesAdherenceRate(round(avgAdherence))
                                .proteinAdherenceRate(round(avgAdherence))
                                .carbsAdherenceRate(round(avgAdherence))
                                .fatAdherenceRate(round(avgAdherence))
                                .overallAdherenceRate(round(avgAdherence))
                                .monthsMetGoals(monthsMetGoals)
                                .build();
        }

        private YearlyStatsResponse.YearlyTrends calculateYearlyTrends(
                        List<YearlyStatsResponse.MonthlyBreakdown> monthlyBreakdowns,
                        YearlyStatsResponse.NutritionGoals goals) {

                if (monthlyBreakdowns.isEmpty()) {
                        return YearlyStatsResponse.YearlyTrends.builder()
                                        .overallTrend("STABLE")
                                        .caloriesTrend("STABLE")
                                        .proteinTrend("STABLE")
                                        .carbsTrend("STABLE")
                                        .fatTrend("STABLE")
                                        .averageCalorieDeficit(0.0)
                                        .macroDistribution(YearlyStatsResponse.MacroDistribution.builder()
                                                        .proteinPercentage(0.0).carbsPercentage(0.0).fatPercentage(0.0)
                                                        .build())
                                        .mostConsistentMonth(null)
                                        .leastConsistentMonth(null)
                                        .build();
                }

                // Calculate trends
                String caloriesTrend = calculateTrend(monthlyBreakdowns,
                                YearlyStatsResponse.MonthlyBreakdown::getAverageCalories);
                String proteinTrend = calculateTrend(monthlyBreakdowns,
                                YearlyStatsResponse.MonthlyBreakdown::getAverageProtein);
                String carbsTrend = calculateTrend(monthlyBreakdowns,
                                YearlyStatsResponse.MonthlyBreakdown::getAverageCarbs);
                String fatTrend = calculateTrend(monthlyBreakdowns,
                                YearlyStatsResponse.MonthlyBreakdown::getAverageFat);

                // Overall trend based on adherence
                String overallTrend = calculateTrend(monthlyBreakdowns,
                                YearlyStatsResponse.MonthlyBreakdown::getAdherenceRate);

                // Calculate average calorie deficit
                double avgCalories = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAverageCalories).average()
                                .orElse(0.0);
                double avgDeficit = goals.getDailyCalories() - avgCalories;

                // Calculate macro distribution
                double avgProtein = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAverageProtein).average()
                                .orElse(0.0);
                double avgCarbs = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAverageCarbs).average()
                                .orElse(0.0);
                double avgFat = monthlyBreakdowns.stream()
                                .mapToDouble(YearlyStatsResponse.MonthlyBreakdown::getAverageFat).average().orElse(0.0);

                double proteinCals = avgProtein * 4;
                double carbsCals = avgCarbs * 4;
                double fatCals = avgFat * 9;
                double totalCals = proteinCals + carbsCals + fatCals;

                YearlyStatsResponse.MacroDistribution macroDistribution = YearlyStatsResponse.MacroDistribution
                                .builder()
                                .proteinPercentage(totalCals > 0 ? round((proteinCals / totalCals) * 100) : 0.0)
                                .carbsPercentage(totalCals > 0 ? round((carbsCals / totalCals) * 100) : 0.0)
                                .fatPercentage(totalCals > 0 ? round((fatCals / totalCals) * 100) : 0.0)
                                .build();

                // Find most and least consistent months
                YearlyStatsResponse.MonthlyBreakdown mostConsistent = monthlyBreakdowns.stream()
                                .max(Comparator.comparingDouble(YearlyStatsResponse.MonthlyBreakdown::getAdherenceRate))
                                .orElse(null);
                YearlyStatsResponse.MonthlyBreakdown leastConsistent = monthlyBreakdowns.stream()
                                .min(Comparator.comparingDouble(YearlyStatsResponse.MonthlyBreakdown::getAdherenceRate))
                                .orElse(null);

                return YearlyStatsResponse.YearlyTrends.builder()
                                .overallTrend(overallTrend)
                                .caloriesTrend(caloriesTrend)
                                .proteinTrend(proteinTrend)
                                .carbsTrend(carbsTrend)
                                .fatTrend(fatTrend)
                                .averageCalorieDeficit(round(avgDeficit))
                                .macroDistribution(macroDistribution)
                                .mostConsistentMonth(mostConsistent != null ? mostConsistent.getMonth() : null)
                                .leastConsistentMonth(leastConsistent != null ? leastConsistent.getMonth() : null)
                                .build();
        }

        private YearlyStatsResponse.MonthSummary findBestMonth(
                        List<YearlyStatsResponse.MonthlyBreakdown> monthlyBreakdowns) {
                if (monthlyBreakdowns.isEmpty()) {
                        return null;
                }

                YearlyStatsResponse.MonthlyBreakdown best = monthlyBreakdowns.stream()
                                .max(Comparator.comparingDouble(YearlyStatsResponse.MonthlyBreakdown::getAdherenceRate))
                                .orElse(monthlyBreakdowns.get(0));

                return YearlyStatsResponse.MonthSummary.builder()
                                .month(best.getMonth())
                                .averageCalories(best.getAverageCalories())
                                .mealsLogged(best.getMealsCount())
                                .reason("Highest goal adherence rate: " + best.getAdherenceRate() + "%")
                                .build();
        }

        private YearlyStatsResponse.MonthSummary findWorstMonth(
                        List<YearlyStatsResponse.MonthlyBreakdown> monthlyBreakdowns) {
                if (monthlyBreakdowns.isEmpty()) {
                        return null;
                }

                YearlyStatsResponse.MonthlyBreakdown worst = monthlyBreakdowns.stream()
                                .min(Comparator.comparingDouble(YearlyStatsResponse.MonthlyBreakdown::getAdherenceRate))
                                .orElse(monthlyBreakdowns.get(monthlyBreakdowns.size() - 1));

                return YearlyStatsResponse.MonthSummary.builder()
                                .month(worst.getMonth())
                                .averageCalories(worst.getAverageCalories())
                                .mealsLogged(worst.getMealsCount())
                                .reason("Lowest goal adherence rate: " + worst.getAdherenceRate() + "%")
                                .build();
        }

        private YearlyStatsResponse.SeasonalPatterns calculateSeasonalPatterns(
                        Map<YearMonth, List<MealImage>> mealsByMonth) {

                Map<String, List<MealImage>> seasonalMeals = new HashMap<>();
                seasonalMeals.put("winter", new ArrayList<>());
                seasonalMeals.put("spring", new ArrayList<>());
                seasonalMeals.put("summer", new ArrayList<>());
                seasonalMeals.put("autumn", new ArrayList<>());

                for (Map.Entry<YearMonth, List<MealImage>> entry : mealsByMonth.entrySet()) {
                        int month = entry.getKey().getMonthValue();
                        String season = getSeason(month);
                        seasonalMeals.get(season).addAll(entry.getValue());
                }

                return YearlyStatsResponse.SeasonalPatterns.builder()
                                .winter(calculateSeasonStats(seasonalMeals.get("winter")))
                                .spring(calculateSeasonStats(seasonalMeals.get("spring")))
                                .summer(calculateSeasonStats(seasonalMeals.get("summer")))
                                .autumn(calculateSeasonStats(seasonalMeals.get("autumn")))
                                .build();
        }

        private YearlyStatsResponse.SeasonStats calculateSeasonStats(List<MealImage> meals) {
                if (meals.isEmpty()) {
                        return YearlyStatsResponse.SeasonStats.builder()
                                        .averageCalories(0.0).averageProtein(0.0)
                                        .averageCarbs(0.0).averageFat(0.0).totalMeals(0).build();
                }

                Map<LocalDate, List<MealImage>> mealsByDate = meals.stream()
                                .collect(Collectors.groupingBy(meal -> meal.getUploadedAt().toLocalDate()));

                double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
                for (MealImage meal : meals) {
                        if (meal.getNutritionSummary() != null) {
                                MealImage.NutritionSummary summary = meal.getNutritionSummary();
                                totalCalories += summary.getTotalCalories() != null ? summary.getTotalCalories() : 0;
                                totalProtein += summary.getTotalProtein() != null ? summary.getTotalProtein() : 0;
                                totalCarbs += summary.getTotalCarbohydrates() != null ? summary.getTotalCarbohydrates()
                                                : 0;
                                totalFat += summary.getTotalFat() != null ? summary.getTotalFat() : 0;
                        }
                }

                int daysLogged = mealsByDate.size();
                return YearlyStatsResponse.SeasonStats.builder()
                                .averageCalories(daysLogged > 0 ? round(totalCalories / daysLogged) : 0.0)
                                .averageProtein(daysLogged > 0 ? round(totalProtein / daysLogged) : 0.0)
                                .averageCarbs(daysLogged > 0 ? round(totalCarbs / daysLogged) : 0.0)
                                .averageFat(daysLogged > 0 ? round(totalFat / daysLogged) : 0.0)
                                .totalMeals(meals.size())
                                .build();
        }

        // ==================== Utility Methods ====================

        private <T> String calculateTrend(List<T> items, java.util.function.ToDoubleFunction<T> valueExtractor) {
                if (items.size() < 2) {
                        return "STABLE";
                }

                List<Double> values = items.stream().map(valueExtractor::applyAsDouble).collect(Collectors.toList());

                // Simple linear regression slope
                int n = values.size();
                double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

                for (int i = 0; i < n; i++) {
                        sumX += i;
                        sumY += values.get(i);
                        sumXY += i * values.get(i);
                        sumX2 += i * i;
                }

                double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);

                // Determine trend based on slope
                if (slope > 5) {
                        return "INCREASING";
                } else if (slope < -5) {
                        return "DECREASING";
                } else {
                        return "STABLE";
                }
        }

        private boolean isWithinGoalRange(double actual, double goal, double tolerance) {
                double lowerBound = goal * (1 - tolerance);
                double upperBound = goal * (1 + tolerance);
                return actual >= lowerBound && actual <= upperBound;
        }

        private String getSeason(int month) {
                if (month == 12 || month == 1 || month == 2) {
                        return "winter";
                } else if (month >= 3 && month <= 5) {
                        return "spring";
                } else if (month >= 6 && month <= 8) {
                        return "summer";
                } else {
                        return "autumn";
                }
        }

        private double round(double value) {
                return Math.round(value * 100.0) / 100.0;
        }
}
