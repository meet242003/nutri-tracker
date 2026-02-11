// API Types
export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
    profileUrl?: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    id: string;
    name: string;
    email: string;
    profileUrl?: string;
    emailVerified: boolean;
    token?: string;
}

export interface MealImageUploadResponse {
    id: string;
    userId: string;
    imageUrl: string;
    fileName: string;
    status: string;
    uploadedAt: string;
}

export interface NutritionInfo {
    calories: number;
    protein: number;
    carbohydrates: number;
    fat: number;
    fiber: number;
    sugar: number;
}

export interface IngredientInfo {
    name: string;
    quantityGrams: number;
    category: string;
    nutrition: NutritionInfo;
}

export interface FoodItem {
    name: string;
    confidence: number;
    quantity: number;
    nutrition: NutritionInfo;
    visualCues: string;
    category: string;
    ingredientBreakdown: IngredientInfo[];
}

export interface NutritionSummary {
    totalCalories: number;
    totalProtein: number;
    totalCarbohydrates: number;
    totalFat: number;
    totalFiber: number;
    totalSugar: number;
}

export interface MealImage {
    id: string;
    userId: string;
    imageUrl: string;
    fileName: string;
    status: string;
    detectedFoods: FoodItem[];
    nutritionSummary: NutritionSummary;
    uploadedAt: string;
    analyzedAt?: string;
    errorMessage?: string;
}

// User Profile Types
export interface UserProfileRequest {
    name?: string;
    height?: number; // in cm
    weight?: number; // in kg
    dateOfBirth?: string; // ISO date string
    gender?: 'MALE' | 'FEMALE' | 'OTHER';
    activityLevel?: 'SEDENTARY' | 'LIGHTLY_ACTIVE' | 'MODERATELY_ACTIVE' | 'VERY_ACTIVE' | 'EXTREMELY_ACTIVE';
    goal?: 'WEIGHT_LOSS' | 'MUSCLE_GAIN' | 'WEIGHT_GAIN' | 'MAINTENANCE';
}

export interface NutritionGoals {
    calories: number;
    protein: number;
    carbohydrates: number;
    fat: number;
}

export interface UserProfileResponse {
    id: string;
    name: string;
    email: string;
    profileUrl?: string;
    height?: number;
    weight?: number;
    dateOfBirth?: string;
    age?: number;
    gender?: string;
    activityLevel?: string;
    goal?: string;
    bmr?: number;
    tdee?: number;
    nutritionGoals?: NutritionGoals;
    createdAt: string;
    updatedAt: string;
}

// Daily Stats Types
export interface DailyNutrition {
    calories: number;
    protein: number;
    carbohydrates: number;
    fat: number;
    fiber: number;
    sugar: number;
}

export interface MealSummary {
    id: string;
    imageUrl: string | null;
    uploadedAt: string;
    nutrition: DailyNutrition;
    foodItems: string[];
}

export interface DailyStatsResponse {
    date: string;
    consumed: DailyNutrition;
    goals: NutritionGoals;
    remaining: Omit<NutritionGoals, 'fiber' | 'sugar'>;
    meals: MealSummary[];
    totalMeals: number;
}

// Manual Food Entry Types
export interface FoodSearchItem {
    id: string;
    name: string;
    category: string;
    nutritionPer100g: {
        calories: number;
        protein: number;
        carbohydrates: number;
        fat: number;
        fiber: number;
        sugar: number;
    };
}

export interface FoodSearchResponse {
    results: FoodSearchItem[];
    totalResults: number;
}

export interface ManualFoodEntry {
    name: string;
    quantityGrams: number;
    nutrition: {
        calories: number;
        protein: number;
        carbohydrates: number;
        fat: number;
        fiber: number;
        sugar: number;
    };
}

export interface ManualFoodEntryRequest {
    foods: ManualFoodEntry[];
}
