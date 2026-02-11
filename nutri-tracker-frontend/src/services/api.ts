import type {
    RegisterRequest,
    LoginRequest,
    AuthResponse,
    MealImageUploadResponse,
    MealImage,
    UserProfileRequest,
    UserProfileResponse,
    DailyStatsResponse,
    FoodSearchResponse,
    ManualFoodEntryRequest,
} from '../types';

const API_BASE_URL = 'http://localhost:8080/api';

// Helper function to get auth token
const getAuthToken = (): string | null => {
    return localStorage.getItem('authToken');
};

// Helper function to create headers
const createHeaders = (includeAuth = false): HeadersInit => {
    const headers: HeadersInit = {
        'Content-Type': 'application/json',
    };

    if (includeAuth) {
        const token = getAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
    }

    return headers;
};

// Auth API
export const authApi = {
    register: async (data: RegisterRequest): Promise<AuthResponse> => {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: createHeaders(),
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Registration failed');
        }

        return response.json();
    },

    login: async (data: LoginRequest): Promise<AuthResponse> => {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: createHeaders(),
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Login failed');
        }

        const result = await response.json();

        // Store token if present
        if (result.token) {
            localStorage.setItem('authToken', result.token);
        }

        return result;
    },

    logout: () => {
        localStorage.removeItem('authToken');
    },
};

// User Profile API
export const userApi = {
    getProfile: async (): Promise<UserProfileResponse> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/user/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error('Failed to fetch user profile');
        }

        return response.json();
    },

    updateProfile: async (data: UserProfileRequest): Promise<UserProfileResponse> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/user/profile`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to update profile');
        }

        return response.json();
    },
};

// Stats API
export const statsApi = {
    getTodayStats: async (): Promise<DailyStatsResponse> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/stats/today`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error('Failed to fetch today\'s stats');
        }

        return response.json();
    },

    getDailyStats: async (date: string): Promise<DailyStatsResponse> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/stats/daily?date=${date}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error('Failed to fetch daily stats');
        }

        return response.json();
    },
};

// Meals API
export const mealsApi = {
    uploadMealImage: async (file: File): Promise<MealImageUploadResponse> => {
        const formData = new FormData();
        formData.append('image', file);

        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/meals/upload`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
            body: formData,
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Upload failed');
        }

        return response.json();
    },

    searchFoods: async (query: string, limit: number = 10): Promise<FoodSearchResponse> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/meals/search?q=${encodeURIComponent(query)}&limit=${limit}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error('Failed to search foods');
        }

        return response.json();
    },

    createManualEntry: async (data: ManualFoodEntryRequest): Promise<MealImage> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/meals/manual`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to create manual entry');
        }

        return response.json();
    },

    getUserMealImages: async (): Promise<MealImage[]> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/meals`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error('Failed to fetch meal images');
        }

        return response.json();
    },

    getMealAnalysis: async (mealId: string): Promise<MealImage> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/meals/${mealId}/analysis`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error('Failed to fetch meal analysis');
        }

        return response.json();
    },

    deleteMealImage: async (mealId: string): Promise<void> => {
        const token = getAuthToken();
        const response = await fetch(`${API_BASE_URL}/meals/${mealId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error('Failed to delete meal image');
        }
    },
};
