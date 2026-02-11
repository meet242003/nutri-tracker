package com.project.NutriTracker.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.project.NutriTracker.document.MealImage;

@Repository
public interface MealImageRepository extends MongoRepository<MealImage, String> {
    List<MealImage> findByUserId(String userId);

    List<MealImage> findByUserIdOrderByUploadedAtDesc(String userId);

    List<MealImage> findByStatus(String status);

    List<MealImage> findByUserIdAndUploadedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
}
