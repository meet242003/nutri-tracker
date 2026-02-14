import api from './api';

export interface MealImageUploadResponse {
    id: string;
    imageUrl: string;
    status: string;
    message: string;
    uploadedAt: string;
}

export interface NutritionInfo {
    calories: number;
    protein: number;
    carbohydrates: number;
    fat: number;
    fiber?: number;
    sugar?: number;
}

export interface MealSummary {
    id: string;
    imageUrl: string;
    uploadedAt: string;
    nutrition: NutritionInfo;
    foodItems: string[];
}

export interface DailyStatsResponse {
    date: string;
    consumed: NutritionInfo;
    goals: {
        calories: number;
        protein: number;
        carbohydrates: number;
        fat: number;
    };
    remaining: {
        calories: number;
        protein: number;
        carbohydrates: number;
        fat: number;
    };
    meals: MealSummary[];
    totalMeals: number;
}

export interface FoodItem {
    id: string;
    name: string;
    category: string;
    nutritionPer100g: NutritionInfo;
}

export interface FoodSearchResponse {
    results: FoodItem[];
    totalResults: number;
}

export interface ManualFoodEntry {
    foodId: string;
    quantity: number; // in grams
}

export interface ManualFoodEntryRequest {
    foods: ManualFoodEntry[];
}

// Upload meal image
export const uploadMealImage = async (file: File) => {
    const formData = new FormData();
    formData.append('image', file);

    const response = await api.post<MealImageUploadResponse>('/meals/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.data;
};

// Get today's stats
export const getTodayStats = async () => {
    const response = await api.get<DailyStatsResponse>('/stats/today');
    return response.data;
};

// Get daily stats for a specific date
export const getDailyStats = async (date: string) => {
    const response = await api.get<DailyStatsResponse>(`/stats/daily?date=${date}`);
    return response.data;
};

// Search for foods
export const searchFoods = async (query: string, limit: number = 10) => {
    const response = await api.get<FoodSearchResponse>(`/meals/search?q=${query}&limit=${limit}`);
    return response.data;
};

// Create manual food entry
export const createManualEntry = async (foods: ManualFoodEntry[]) => {
    const response = await api.post('/meals/manual', { foods });
    return response.data;
};

// Get all user meals
export const getUserMeals = async () => {
    const response = await api.get<MealSummary[]>('/meals');
    return response.data;
};

// Delete meal
export const deleteMeal = async (mealId: string) => {
    const response = await api.delete(`/meals/${mealId}`);
    return response.data;
};
