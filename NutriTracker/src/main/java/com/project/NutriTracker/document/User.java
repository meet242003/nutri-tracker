package com.project.NutriTracker.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private String profileUrl;
    private boolean emailVerified;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    // Health Profile Fields
    private Double height; // in cm
    private Double weight; // in kg
    private java.time.LocalDate dateOfBirth;
    private String gender; // MALE, FEMALE, OTHER
    private String activityLevel; // SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE, EXTREMELY_ACTIVE
    private String goal; // WEIGHT_LOSS, WEIGHT_GAIN, MUSCLE_GAIN, MAINTENANCE

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
