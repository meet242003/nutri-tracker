import { useState } from 'react';
import { type UserProfileRequest } from '../services/user.service';

interface OnboardingFormProps {
    onSubmit: (data: UserProfileRequest) => Promise<void>;
}

export const OnboardingForm = ({ onSubmit }: OnboardingFormProps) => {
    const [step, setStep] = useState(1);
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState<UserProfileRequest>({
        name: '',
        height: 0,
        weight: 0,
        dateOfBirth: '',
        gender: 'MALE',
        activityLevel: 'SEDENTARY',
        goal: 'MAINTENANCE'
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'height' || name === 'weight' ? parseFloat(value) : value
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            await onSubmit(formData);
        } catch (error) {
            console.error('Error submitting onboarding form:', error);
        } finally {
            setLoading(false);
        }
    };

    const nextStep = () => setStep(prev => prev + 1);
    const prevStep = () => setStep(prev => prev - 1);

    const renderStep = () => {
        switch (step) {
            case 1:
                return (
                    <div className="space-y-4 animate-fadeIn">
                        <h3 className="text-2xl font-bold text-white mb-6">Let's get to know you</h3>

                        <div>
                            <label className="block text-sm font-medium text-neutral-300 mb-2">Full Name</label>
                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white focus:ring-2 focus:ring-emerald-500"
                                placeholder="Your Name"
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-neutral-300 mb-2">Gender</label>
                                <select
                                    name="gender"
                                    value={formData.gender}
                                    onChange={handleChange}
                                    className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white focus:ring-2 focus:ring-emerald-500"
                                >
                                    <option value="MALE">Male</option>
                                    <option value="FEMALE">Female</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-neutral-300 mb-2">Date of Birth</label>
                                <input
                                    type="date"
                                    name="dateOfBirth"
                                    value={formData.dateOfBirth}
                                    onChange={handleChange}
                                    className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white focus:ring-2 focus:ring-emerald-500"
                                />
                            </div>
                        </div>

                        <button
                            type="button"
                            onClick={nextStep}
                            className="w-full mt-6 px-6 py-3 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold rounded-lg transition-colors"
                        >
                            Next
                        </button>
                    </div>
                );
            case 2:
                return (
                    <div className="space-y-4 animate-fadeIn">
                        <h3 className="text-2xl font-bold text-white mb-6">Body Measurements</h3>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-neutral-300 mb-2">Height (cm)</label>
                                <input
                                    type="number"
                                    name="height"
                                    value={formData.height || ''}
                                    onChange={handleChange}
                                    className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white focus:ring-2 focus:ring-emerald-500"
                                    placeholder="175"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-neutral-300 mb-2">Weight (kg)</label>
                                <input
                                    type="number"
                                    name="weight"
                                    value={formData.weight || ''}
                                    onChange={handleChange}
                                    className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white focus:ring-2 focus:ring-emerald-500"
                                    placeholder="70"
                                />
                            </div>
                        </div>

                        <div className="flex gap-4 mt-6">
                            <button
                                type="button"
                                onClick={prevStep}
                                className="flex-1 px-6 py-3 bg-neutral-700 hover:bg-neutral-600 text-white font-semibold rounded-lg transition-colors"
                            >
                                Back
                            </button>
                            <button
                                type="button"
                                onClick={nextStep}
                                className="flex-1 px-6 py-3 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold rounded-lg transition-colors"
                            >
                                Next
                            </button>
                        </div>
                    </div>
                );
            case 3:
                return (
                    <div className="space-y-4 animate-fadeIn">
                        <h3 className="text-2xl font-bold text-white mb-6">Goals & Activity</h3>

                        <div>
                            <label className="block text-sm font-medium text-neutral-300 mb-2">Activity Level</label>
                            <select
                                name="activityLevel"
                                value={formData.activityLevel}
                                onChange={handleChange}
                                className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white focus:ring-2 focus:ring-emerald-500"
                            >
                                <option value="SEDENTARY">Sedentary (Little or no exercise)</option>
                                <option value="LIGHTLY_ACTIVE">Lightly Active (Light exercise/sports 1-3 days/week)</option>
                                <option value="MODERATELY_ACTIVE">Moderately Active (Moderate exercise/sports 3-5 days/week)</option>
                                <option value="VERY_ACTIVE">Very Active (Hard exercise/sports 6-7 days/week)</option>
                                <option value="EXTREMELY_ACTIVE">Extremely Active (Very hard exercise & physical job)</option>
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-neutral-300 mb-2">Your Goal</label>
                            <select
                                name="goal"
                                value={formData.goal}
                                onChange={handleChange}
                                className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white focus:ring-2 focus:ring-emerald-500"
                            >
                                <option value="WEIGHT_LOSS">Weight Loss</option>
                                <option value="MAINTENANCE">Maintenance</option>
                                <option value="MUSCLE_GAIN">Muscle Gain</option>
                                <option value="WEIGHT_GAIN">Weight Gain</option>
                            </select>
                        </div>

                        <div className="flex gap-4 mt-6">
                            <button
                                type="button"
                                onClick={prevStep}
                                className="flex-1 px-6 py-3 bg-neutral-700 hover:bg-neutral-600 text-white font-semibold rounded-lg transition-colors"
                            >
                                Back
                            </button>
                            <button
                                type="submit"
                                disabled={loading}
                                className="flex-1 px-6 py-3 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-semibold rounded-lg transition-transform transform hover:scale-105 disabled:opacity-50"
                            >
                                {loading ? 'Saving...' : 'Complete Profile'}
                            </button>
                        </div>
                    </div>
                );
            default:
                return null;
        }
    };

    return (
        <div className="max-w-md w-full mx-auto bg-neutral-900 border border-neutral-800 rounded-2xl p-8 shadow-2xl">
            {/* Progress Bar */}
            <div className="flex justify-between mb-8">
                {[1, 2, 3].map((s) => (
                    <div
                        key={s}
                        className={`h-2 flex-1 mx-1 rounded-full transition-colors ${s <= step ? 'bg-emerald-500' : 'bg-neutral-700'
                            }`}
                    />
                ))}
            </div>

            <form onSubmit={handleSubmit}>
                {renderStep()}
            </form>
        </div>
    );
};
