import { useState, useEffect } from 'react';
import { statsApi, userApi } from '../services/api';
import type { DailyStatsResponse, UserProfileResponse } from '../types';

export default function Dashboard() {
    const [stats, setStats] = useState<DailyStatsResponse | null>(null);
    const [profile, setProfile] = useState<UserProfileResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            const [statsData, profileData] = await Promise.all([
                statsApi.getTodayStats(),
                userApi.getProfile(),
            ]);
            setStats(statsData);
            setProfile(profileData);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load data');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-600"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
                    {error}
                </div>
            </div>
        );
    }

    if (!stats) return null;

    const calorieProgress = (stats.consumed.calories / stats.goals.calories) * 100;
    const proteinProgress = (stats.consumed.protein / stats.goals.protein) * 100;
    const carbsProgress = (stats.consumed.carbohydrates / stats.goals.carbohydrates) * 100;
    const fatProgress = (stats.consumed.fat / stats.goals.fat) * 100;

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {/* Header Stats */}
            <div className="mb-8">
                <h2 className="text-2xl font-bold text-gray-800 mb-6">Today's Nutrition</h2>

                {/* Main Calorie Card */}
                <div className="bg-white rounded-2xl shadow-lg p-6 mb-6">
                    <div className="flex items-center justify-between mb-4">
                        <div>
                            <h3 className="text-lg font-semibold text-gray-700">Calories</h3>
                            <p className="text-3xl font-bold text-emerald-600">
                                {Math.round(stats.consumed.calories)}
                                <span className="text-lg text-gray-500"> / {Math.round(stats.goals.calories)}</span>
                            </p>
                        </div>
                        <div className="text-right">
                            <p className="text-sm text-gray-600">Remaining</p>
                            <p className="text-2xl font-bold text-gray-800">
                                {Math.round(stats.remaining.calories)}
                            </p>
                        </div>
                    </div>

                    {/* Progress Bar */}
                    <div className="w-full bg-gray-200 rounded-full h-3">
                        <div
                            className={`h-3 rounded-full transition-all ${calorieProgress > 100 ? 'bg-red-500' : 'bg-emerald-500'
                                }`}
                            style={{ width: `${Math.min(calorieProgress, 100)}%` }}
                        ></div>
                    </div>
                </div>

                {/* Macros Grid */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {/* Protein */}
                    <div className="bg-white rounded-xl shadow p-5">
                        <div className="flex items-center justify-between mb-3">
                            <h4 className="text-sm font-semibold text-gray-600">Protein</h4>
                            <span className="text-xs text-gray-500">
                                {Math.round(proteinProgress)}%
                            </span>
                        </div>
                        <p className="text-2xl font-bold text-blue-600 mb-2">
                            {Math.round(stats.consumed.protein)}g
                            <span className="text-sm text-gray-500"> / {Math.round(stats.goals.protein)}g</span>
                        </p>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                            <div
                                className="bg-blue-500 h-2 rounded-full transition-all"
                                style={{ width: `${Math.min(proteinProgress, 100)}%` }}
                            ></div>
                        </div>
                    </div>

                    {/* Carbs */}
                    <div className="bg-white rounded-xl shadow p-5">
                        <div className="flex items-center justify-between mb-3">
                            <h4 className="text-sm font-semibold text-gray-600">Carbs</h4>
                            <span className="text-xs text-gray-500">
                                {Math.round(carbsProgress)}%
                            </span>
                        </div>
                        <p className="text-2xl font-bold text-amber-600 mb-2">
                            {Math.round(stats.consumed.carbohydrates)}g
                            <span className="text-sm text-gray-500"> / {Math.round(stats.goals.carbohydrates)}g</span>
                        </p>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                            <div
                                className="bg-amber-500 h-2 rounded-full transition-all"
                                style={{ width: `${Math.min(carbsProgress, 100)}%` }}
                            ></div>
                        </div>
                    </div>

                    {/* Fat */}
                    <div className="bg-white rounded-xl shadow p-5">
                        <div className="flex items-center justify-between mb-3">
                            <h4 className="text-sm font-semibold text-gray-600">Fat</h4>
                            <span className="text-xs text-gray-500">
                                {Math.round(fatProgress)}%
                            </span>
                        </div>
                        <p className="text-2xl font-bold text-purple-600 mb-2">
                            {Math.round(stats.consumed.fat)}g
                            <span className="text-sm text-gray-500"> / {Math.round(stats.goals.fat)}g</span>
                        </p>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                            <div
                                className="bg-purple-500 h-2 rounded-full transition-all"
                                style={{ width: `${Math.min(fatProgress, 100)}%` }}
                            ></div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Profile Summary */}
            {profile?.nutritionGoals && (
                <div className="bg-white rounded-xl shadow p-6 mb-8">
                    <h3 className="text-lg font-semibold text-gray-800 mb-4">Your Goals</h3>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                        <div>
                            <p className="text-sm text-gray-600">BMR</p>
                            <p className="text-xl font-bold text-gray-800">{Math.round(profile.bmr || 0)}</p>
                            <p className="text-xs text-gray-500">kcal/day</p>
                        </div>
                        <div>
                            <p className="text-sm text-gray-600">TDEE</p>
                            <p className="text-xl font-bold text-gray-800">{Math.round(profile.tdee || 0)}</p>
                            <p className="text-xs text-gray-500">kcal/day</p>
                        </div>
                        <div>
                            <p className="text-sm text-gray-600">Activity</p>
                            <p className="text-sm font-semibold text-gray-800">
                                {profile.activityLevel?.replace(/_/g, ' ')}
                            </p>
                        </div>
                        <div>
                            <p className="text-sm text-gray-600">Goal</p>
                            <p className="text-sm font-semibold text-gray-800">
                                {profile.goal?.replace(/_/g, ' ')}
                            </p>
                        </div>
                    </div>
                </div>
            )}

            {/* Meals List */}
            <div className="bg-white rounded-xl shadow p-6">
                <h3 className="text-lg font-semibold text-gray-800 mb-4">
                    Today's Meals ({stats.totalMeals})
                </h3>

                {stats.meals.length === 0 ? (
                    <p className="text-gray-500 text-center py-8">No meals logged yet today</p>
                ) : (
                    <div className="space-y-4">
                        {stats.meals.map((meal) => (
                            <div key={meal.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                                <div className="flex items-start gap-4">
                                    {meal.imageUrl && (
                                        <img
                                            src={meal.imageUrl}
                                            alt="Meal"
                                            className="w-20 h-20 rounded-lg object-cover"
                                        />
                                    )}
                                    <div className="flex-1">
                                        <div className="flex items-start justify-between mb-2">
                                            <div>
                                                <p className="font-medium text-gray-800">
                                                    {meal.foodItems.join(', ') || 'Manual Entry'}
                                                </p>
                                                <p className="text-sm text-gray-500">
                                                    {new Date(meal.uploadedAt).toLocaleTimeString()}
                                                </p>
                                            </div>
                                            <p className="text-lg font-bold text-emerald-600">
                                                {Math.round(meal.nutrition.calories)} kcal
                                            </p>
                                        </div>
                                        <div className="flex gap-4 text-sm text-gray-600">
                                            <span>P: {Math.round(meal.nutrition.protein)}g</span>
                                            <span>C: {Math.round(meal.nutrition.carbohydrates)}g</span>
                                            <span>F: {Math.round(meal.nutrition.fat)}g</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
