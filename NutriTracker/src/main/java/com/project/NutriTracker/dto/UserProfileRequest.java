package com.project.NutriTracker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    private String name;
    private Double height; // in cm
    private Double weight; // in kg
    private LocalDate dateOfBirth;
    private String gender; // MALE, FEMALE, OTHER
    private String activityLevel; // SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE, EXTREMELY_ACTIVE
    private String goal; // WEIGHT_LOSS, WEIGHT_GAIN, MUSCLE_GAIN, MAINTENANCE
}
