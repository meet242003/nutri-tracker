package com.project.NutriTracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.project.NutriTracker.document.FoodComposition;

public interface FoodCompositionRepository extends MongoRepository<FoodComposition, String> {

    Optional<FoodComposition> findByCode(String code);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<FoodComposition> findByNameContainingIgnoreCase(String name);

    // Find exact name match (case insensitive)
    @Query("{ 'name': { $regex: '^?0$', $options: 'i' } }")
    Optional<FoodComposition> findByNameIgnoreCase(String name);
}
