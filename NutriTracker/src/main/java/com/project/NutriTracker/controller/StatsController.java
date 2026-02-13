package com.project.NutriTracker.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.NutriTracker.document.User;
import com.project.NutriTracker.dto.DailyStatsResponse;
import com.project.NutriTracker.service.StatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    /**
     * Get daily nutrition stats
     * GET /api/stats/daily?date=2026-02-11
     * If no date provided, returns today's stats
     */
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        try {
            User principal = (User) authentication.getPrincipal();
            String email = principal.getEmail();
            LocalDate targetDate = date != null ? date : LocalDate.now();

            log.info("Fetching daily stats for user: {} on date: {}", email, targetDate);

            DailyStatsResponse stats = statsService.getDailyStats(email, targetDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching daily stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch daily stats: " + e.getMessage()));
        }
    }

    /**
     * Get today's nutrition stats (convenience endpoint)
     * GET /api/stats/today
     */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayStats(Authentication authentication) {
        try {
            User principal = (User) authentication.getPrincipal();
            String email = principal.getEmail();
            log.info("Fetching today's stats for user: {}", email);

            DailyStatsResponse stats = statsService.getDailyStats(email, LocalDate.now());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching today's stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch today's stats: " + e.getMessage()));
        }
    }

    /**
     * Get monthly nutrition stats
     * GET /api/stats/monthly?year=2026&month=2
     */
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyStats(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        try {
            User principal = (User) authentication.getPrincipal();
            String email = principal.getEmail();
            log.info("Fetching monthly stats for user: {} for {}-{}", email, year, month);

            com.project.NutriTracker.dto.MonthlyStatsResponse stats = statsService.getMonthlyStats(email, year, month);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching monthly stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch monthly stats: " + e.getMessage()));
        }
    }

    /**
     * Get yearly nutrition stats
     * GET /api/stats/yearly?year=2026
     */
    @GetMapping("/yearly")
    public ResponseEntity<?> getYearlyStats(
            @RequestParam int year,
            Authentication authentication) {
        try {
            User principal = (User) authentication.getPrincipal();
            String email = principal.getEmail();
            log.info("Fetching yearly stats for user: {} for year {}", email, year);

            com.project.NutriTracker.dto.YearlyStatsResponse stats = statsService.getYearlyStats(email, year);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching yearly stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Failed to fetch yearly stats: " + e.getMessage()));
        }
    }

    // Inner class for error responses
    private record ErrorResponse(String error) {
    }
}
