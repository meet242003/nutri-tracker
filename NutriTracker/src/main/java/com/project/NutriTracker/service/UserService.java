package com.project.NutriTracker.service;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;

import com.project.NutriTracker.document.User;
import com.project.NutriTracker.dto.UserProfileRequest;
import com.project.NutriTracker.dto.UserProfileResponse;
import com.project.NutriTracker.exception.ResourceNotFoundException;
import com.project.NutriTracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get user profile by email
     */
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildUserProfileResponse(user);
    }

    /**
     * Update user profile
     */
    public UserProfileResponse updateUserProfile(String email, UserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update user fields
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        if (request.getWeight() != null) {
            user.setWeight(request.getWeight());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getActivityLevel() != null) {
            user.setActivityLevel(request.getActivityLevel());
        }
        if (request.getGoal() != null) {
            user.setGoal(request.getGoal());
        }

        user = userRepository.save(user);
        log.info("Updated profile for user: {}", email);

        return buildUserProfileResponse(user);
    }

    /**
     * Build UserProfileResponse with calculated values
     */
    private UserProfileResponse buildUserProfileResponse(User user) {
        Integer age = calculateAge(user.getDateOfBirth());
        Double bmr = calculateBMR(user.getWeight(), user.getHeight(), age, user.getGender());
        Double tdee = calculateTDEE(bmr, user.getActivityLevel());
        UserProfileResponse.NutritionGoals goals = calculateNutritionGoals(tdee, user.getGoal());

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileUrl(user.getProfileUrl())
                .height(user.getHeight())
                .weight(user.getWeight())
                .dateOfBirth(user.getDateOfBirth())
                .age(age)
                .gender(user.getGender())
                .activityLevel(user.getActivityLevel())
                .goal(user.getGoal())
                .bmr(bmr)
                .tdee(tdee)
                .nutritionGoals(goals)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Calculate age from date of birth
     */
    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor Equation
     * BMR (men) = 10 × weight (kg) + 6.25 × height (cm) - 5 × age (years) + 5
     * BMR (women) = 10 × weight (kg) + 6.25 × height (cm) - 5 × age (years) - 161
     */
    private Double calculateBMR(Double weight, Double height, Integer age, String gender) {
        if (weight == null || height == null || age == null || gender == null) {
            return null;
        }

        double bmr = (10 * weight) + (6.25 * height) - (5 * age);

        if ("MALE".equalsIgnoreCase(gender)) {
            bmr += 5;
        } else if ("FEMALE".equalsIgnoreCase(gender)) {
            bmr -= 161;
        } else {
            // For OTHER, use average
            bmr -= 78;
        }

        return Math.round(bmr * 100.0) / 100.0;
    }

    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     * TDEE = BMR × Activity Factor
     */
    private Double calculateTDEE(Double bmr, String activityLevel) {
        if (bmr == null || activityLevel == null) {
            return null;
        }

        double activityFactor = switch (activityLevel) {
            case "SEDENTARY" -> 1.2;
            case "LIGHTLY_ACTIVE" -> 1.375;
            case "MODERATELY_ACTIVE" -> 1.55;
            case "VERY_ACTIVE" -> 1.725;
            case "EXTREMELY_ACTIVE" -> 1.9;
            default -> 1.2;
        };

        return Math.round(bmr * activityFactor * 100.0) / 100.0;
    }

    /**
     * Calculate nutrition goals based on TDEE and goal
     * Macronutrient ratios:
     * - Weight Loss: 40% protein, 30% carbs, 30% fat (with calorie deficit)
     * - Muscle Gain: 30% protein, 40% carbs, 30% fat (with calorie surplus)
     * - Weight Gain: 25% protein, 45% carbs, 30% fat (with calorie surplus)
     * - Maintenance: 30% protein, 40% carbs, 30% fat
     */
    private UserProfileResponse.NutritionGoals calculateNutritionGoals(Double tdee, String goal) {
        if (tdee == null) {
            return null;
        }

        double targetCalories = tdee;
        double proteinRatio = 0.30;
        double carbRatio = 0.40;
        double fatRatio = 0.30;

        if (goal != null) {
            switch (goal) {
                case "WEIGHT_LOSS":
                    targetCalories = tdee * 0.85; // 15% deficit
                    proteinRatio = 0.40;
                    carbRatio = 0.30;
                    fatRatio = 0.30;
                    break;
                case "MUSCLE_GAIN":
                    targetCalories = tdee * 1.10; // 10% surplus
                    proteinRatio = 0.30;
                    carbRatio = 0.40;
                    fatRatio = 0.30;
                    break;
                case "WEIGHT_GAIN":
                    targetCalories = tdee * 1.15; // 15% surplus
                    proteinRatio = 0.25;
                    carbRatio = 0.45;
                    fatRatio = 0.30;
                    break;
                case "MAINTENANCE":
                default:
                    // Keep defaults
                    break;
            }
        }

        // Calculate macros (1g protein = 4 cal, 1g carb = 4 cal, 1g fat = 9 cal)
        double protein = Math.round((targetCalories * proteinRatio) / 4 * 100.0) / 100.0;
        double carbs = Math.round((targetCalories * carbRatio) / 4 * 100.0) / 100.0;
        double fat = Math.round((targetCalories * fatRatio) / 9 * 100.0) / 100.0;

        return UserProfileResponse.NutritionGoals.builder()
                .calories(Math.round(targetCalories * 100.0) / 100.0)
                .protein(protein)
                .carbohydrates(carbs)
                .fat(fat)
                .build();
    }
}
