package com.project.NutriTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealImageUploadResponse {
    private String id;
    private String imageUrl;
    private String status;
    private String message;
    private String uploadedAt;
}
