package com.project.NutriTracker.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.NutriTracker.document.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailVerificationToken(String emailVerificationToken);
}
