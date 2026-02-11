import { useState } from 'react';
import { userApi } from '../services/api';
import type { UserProfileRequest } from '../types';

interface ProfileSetupProps {
    onComplete: () => void;
}

export default function ProfileSetup({ onComplete }: ProfileSetupProps) {
    const [formData, setFormData] = useState<UserProfileRequest>({
        height: undefined,
        weight: undefined,
        dateOfBirth: undefined,
        gender: undefined,
        activityLevel: undefined,
        goal: undefined,
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await userApi.updateProfile(formData);
            onComplete();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to update profile');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-emerald-50 to-teal-100 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-xl p-8 max-w-2xl w-full">
                <div className="text-center mb-8">
                    <h2 className="text-3xl font-bold text-gray-800 mb-2">Complete Your Profile</h2>
                    <p className="text-gray-600">Help us personalize your nutrition goals</p>
                </div>

                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* Basic Info */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Height (cm)
                            </label>
                            <input
                                type="number"
                                value={formData.height || ''}
                                onChange={(e) => setFormData({ ...formData, height: parseFloat(e.target.value) })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                                placeholder="175"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Weight (kg)
                            </label>
                            <input
                                type="number"
                                step="0.1"
                                value={formData.weight || ''}
                                onChange={(e) => setFormData({ ...formData, weight: parseFloat(e.target.value) })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                                placeholder="70"
                                required
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Date of Birth
                            </label>
                            <input
                                type="date"
                                value={formData.dateOfBirth || ''}
                                onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Gender
                            </label>
                            <select
                                value={formData.gender || ''}
                                onChange={(e) => setFormData({ ...formData, gender: e.target.value as any })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                                required
                            >
                                <option value="">Select gender</option>
                                <option value="MALE">Male</option>
                                <option value="FEMALE">Female</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>
                    </div>

                    {/* Activity Level */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Activity Level
                        </label>
                        <select
                            value={formData.activityLevel || ''}
                            onChange={(e) => setFormData({ ...formData, activityLevel: e.target.value as any })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                            required
                        >
                            <option value="">Select activity level</option>
                            <option value="SEDENTARY">Sedentary (little or no exercise)</option>
                            <option value="LIGHTLY_ACTIVE">Lightly Active (exercise 1-3 days/week)</option>
                            <option value="MODERATELY_ACTIVE">Moderately Active (exercise 3-5 days/week)</option>
                            <option value="VERY_ACTIVE">Very Active (exercise 6-7 days/week)</option>
                            <option value="EXTREMELY_ACTIVE">Extremely Active (physical job or 2x training)</option>
                        </select>
                    </div>

                    {/* Goal */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Fitness Goal
                        </label>
                        <select
                            value={formData.goal || ''}
                            onChange={(e) => setFormData({ ...formData, goal: e.target.value as any })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                            required
                        >
                            <option value="">Select your goal</option>
                            <option value="WEIGHT_LOSS">Weight Loss</option>
                            <option value="MUSCLE_GAIN">Muscle Gain</option>
                            <option value="WEIGHT_GAIN">Weight Gain</option>
                            <option value="MAINTENANCE">Maintenance</option>
                        </select>
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                    >
                        {loading ? 'Saving...' : 'Complete Setup'}
                    </button>

                    <button
                        type="button"
                        onClick={onComplete}
                        className="w-full text-gray-600 hover:text-gray-800 font-medium py-2"
                    >
                        Skip for now
                    </button>
                </form>
            </div>
        </div>
    );
}
