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
public class DietRecommendationResponse {

    private String recommendation;
    private List<String> suggestedChanges;
    private List<String> suggestedAddOns;
}
