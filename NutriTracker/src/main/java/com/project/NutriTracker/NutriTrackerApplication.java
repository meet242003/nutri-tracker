package com.project.NutriTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NutriTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NutriTrackerApplication.class, args);
	}

}
