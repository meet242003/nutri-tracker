package com.project.NutriTracker.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.project.NutriTracker.document.FoodComposition;
import com.project.NutriTracker.repository.FoodCompositionRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionDatabaseService {

    private final FoodCompositionRepository foodCompositionRepository;

    @PostConstruct
    public void init() {
        if (foodCompositionRepository.count() == 0) {
            log.info("Loading nutrition database from CSV files...");
            loadIfctData();
            loadAnuvaadData();
        } else {
            log.info("Nutrition database already loaded. Skipping initialization.");
        }
    }

    private void loadIfctData() {
        try {
            ClassPathResource resource = new ClassPathResource("ifct2017_compositions.csv");
            try (Reader reader = new InputStreamReader(resource.getInputStream());
                    CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {

                List<String[]> rows = csvReader.readAll();
                List<FoodComposition> foods = new ArrayList<>();

                for (String[] row : rows) {
                    try {
                        FoodComposition food = new FoodComposition();
                        food.setCode(row[0]);
                        food.setName(row[1]);
                        food.setScientificName(row[2]);

                        // Parse values, handling empty or non-numeric strings
                        food.setProtein(parseDouble(row[6]));
                        food.setTotalFat(parseDouble(row[10]));
                        food.setTotalFiber(parseDouble(row[12]));
                        food.setCarbohydrate(parseDouble(row[18]));

                        // Convert kJ to kcal (1 kcal = 4.184 kJ)
                        Double energyKj = parseDouble(row[20]);
                        if (energyKj != null) {
                            food.setEnergyKcal(Math.round(energyKj / 4.184 * 100.0) / 100.0);
                        }

                        food.setSource("IFCT2017");
                        foods.add(food);
                    } catch (Exception e) {
                        log.warn("Error parsing row for food: {}", row.length > 1 ? row[1] : "Unknown", e);
                    }
                }

                foodCompositionRepository.saveAll(foods);
                log.info("Successfully loaded {} food items from IFCT2017", foods.size());

            }
        } catch (Exception e) {
            log.error("Failed to load nutrition database", e);
        }
    }

    private void loadAnuvaadData() {
        try {
            ClassPathResource resource = new ClassPathResource("Anuvaad_INDB_2024.11.csv");
            try (Reader reader = new InputStreamReader(resource.getInputStream());
                    CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {

                List<String[]> rows = csvReader.readAll();
                List<FoodComposition> foods = new ArrayList<>();

                for (String[] row : rows) {
                    try {
                        // Skip if not enough columns
                        if (row.length < 10) {
                            continue;
                        }

                        FoodComposition food = new FoodComposition();
                        food.setCode(row[0]); // food_code
                        food.setName(row[1]); // food_name
                        food.setScientificName(""); // Not available in this CSV

                        // Parse nutrition values (per 100g)
                        // Column indices based on the CSV structure:
                        // 3: energy_kj, 4: energy_kcal, 5: carb_g, 6: protein_g, 7: fat_g, 9: fibre_g
                        food.setEnergyKcal(parseDouble(row[4])); // energy_kcal
                        food.setProtein(parseDouble(row[6])); // protein_g
                        food.setTotalFat(parseDouble(row[7])); // fat_g
                        food.setCarbohydrate(parseDouble(row[5])); // carb_g
                        food.setTotalFiber(parseDouble(row[9])); // fibre_g

                        food.setSource("Anuvaad_INDB_2024");
                        foods.add(food);
                    } catch (Exception e) {
                        log.warn("Error parsing Anuvaad row for food: {}", row.length > 1 ? row[1] : "Unknown", e);
                    }
                }

                foodCompositionRepository.saveAll(foods);
                log.info("Successfully loaded {} food items from Anuvaad INDB 2024.11", foods.size());

            }
        } catch (Exception e) {
            log.error("Failed to load Anuvaad nutrition database", e);
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("-")) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public FoodComposition findFoodByName(String name) {
        // Try strict match first
        Optional<FoodComposition> exact = foodCompositionRepository.findByNameIgnoreCase(name);
        if (exact.isPresent()) {
            return exact.get();
        }

        // Normalize and try again
        String normalized = normalizeFoodName(name);
        List<FoodComposition> matches = foodCompositionRepository.findByNameContainingIgnoreCase(normalized);

        if (!matches.isEmpty()) {
            return matches.get(0);
        }

        // Try common aliases
        String alias = getCommonAlias(name);
        if (alias != null && !alias.equals(name)) {
            return findFoodByName(alias);
        }

        log.warn("No nutrition data found for: {}", name);
        return null;
    }

    private String normalizeFoodName(String name) {
        if (name == null)
            return "";

        return name.toLowerCase()
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("whole ", "")
                .replaceAll("raw ", "")
                .replaceAll("cooked ", "")
                .replaceAll("fresh ", "")
                .replaceAll("dried ", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String getCommonAlias(String name) {
        String lower = name.toLowerCase();

        if (lower.contains("black lentils") || lower.contains("urad dal")) {
            return "Black gram dal";
        } else if (lower.contains("red lentils") || lower.contains("masoor dal")) {
            return "Lentil";
        } else if (lower.contains("yellow lentils") || lower.contains("toor dal") || lower.contains("arhar dal")) {
            return "Red gram dal";
        } else if (lower.contains("chickpeas") || lower.contains("chana")) {
            return "Bengal gram";
        } else if (lower.contains("kidney beans") || lower.contains("rajma")) {
            return "Kidney beans";
        } else if (lower.contains("basmati rice")) {
            return "Rice";
        } else if (lower.contains("wheat flour") || lower.contains("atta")) {
            return "Wheat flour";
        } else if (lower.contains("ghee") || lower.contains("clarified butter")) {
            return "Ghee";
        } else if (lower.contains("paneer") || lower.contains("cottage cheese")) {
            return "Paneer";
        } else if (lower.contains("curd") || lower.contains("yogurt") || lower.contains("dahi")) {
            return "Curd";
        } else if (lower.contains("tomato")) {
            return "Tomato";
        } else if (lower.contains("onion")) {
            return "Onion";
        } else if (lower.contains("potato")) {
            return "Potato";
        } else if (lower.contains("spinach") || lower.contains("palak")) {
            return "Spinach";
        } else if (lower.contains("chicken")) {
            return "Chicken";
        } else if (lower.contains("mutton") || lower.contains("lamb")) {
            return "Mutton";
        }

        return null;
    }
}
