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

export interface MealAnalysisResponse {
    mealImage: MealImage;
    detectedFoods: FoodItem[];
    nutritionSummary: NutritionSummary;
}
