import api from './api';

export interface UserProfileRequest {
    name: string;
    height: number;
    weight: number;
    dateOfBirth: string;
    gender: 'MALE' | 'FEMALE' | 'OTHER';
    activityLevel: 'SEDENTARY' | 'LIGHTLY_ACTIVE' | 'MODERATELY_ACTIVE' | 'VERY_ACTIVE' | 'EXTREMELY_ACTIVE';
    goal: 'WEIGHT_LOSS' | 'WEIGHT_GAIN' | 'MUSCLE_GAIN' | 'MAINTENANCE';
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
    nutritionGoals?: {
        calories: number;
        protein: number;
        carbohydrates: number;
        fat: number;
    };
}

export const getUserProfile = async () => {
    const response = await api.get<UserProfileResponse>('/user/profile');
    return response.data;
};

export const updateUserProfile = async (data: UserProfileRequest) => {
    const response = await api.put<UserProfileResponse>('/user/profile', data);
    return response.data;
};
