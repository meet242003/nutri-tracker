package com.project.NutriTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectedDish {
    private String dishName;
    private Integer portionGrams;
    private Double confidence;
    private String visualCues;
    private String category; // main_course, bread, dessert, beverage, etc.
}
