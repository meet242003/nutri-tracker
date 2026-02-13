package com.project.NutriTracker.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {

    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate streakStartDate;
    private LocalDate lastLoggedDate;
    private List<StreakDay> calendar;
    private String motivationalMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakDay {
        private LocalDate date;
        private Boolean metGoals;
        private Boolean hasData;
    }
}
