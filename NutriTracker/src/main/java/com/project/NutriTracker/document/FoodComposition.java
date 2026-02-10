package com.project.NutriTracker.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "food_composition")
public class FoodComposition {
    @Id
    private String id;

    private String code;
    private String name;
    private String scientificName;
    private Double energyKcal;
    private Double protein;
    private Double totalFat;
    private Double carbohydrate;
    private Double totalFiber;
    private String source;
}
