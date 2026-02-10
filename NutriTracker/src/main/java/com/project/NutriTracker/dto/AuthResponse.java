package com.project.NutriTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String id;
    private String name;
    private String email;
    private String profileUrl;
    private boolean emailVerified;
    private String token;
}
