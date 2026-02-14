import { useState, useEffect } from 'react';
import { getTodayStats, deleteMeal, type DailyStatsResponse } from '../services/meal.service';
import { DailyStats } from './DailyStats';
import { MealCard } from './MealCard';
import { MealUploadModal } from './MealUploadModal';

export const Dashboard = () => {
    const [stats, setStats] = useState<DailyStatsResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [showUploadModal, setShowUploadModal] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadStats = async () => {
        try {
            console.log('Loading stats...');
            setLoading(true);
            const data = await getTodayStats();
            console.log('Stats loaded:', data);
            setStats(data);
            setError(null);
        } catch (err: any) {
            console.error('Error loading stats:', err);
            setError(err.response?.data?.error || 'Failed to load stats');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadStats();
    }, []);

    const handleMealUploadSuccess = () => {
        console.log('Meal upload success - refreshing stats...');
        loadStats(); // Refresh stats after successful upload
    };

    const handleDeleteMeal = async (mealId: string) => {
        if (!confirm('Are you sure you want to delete this meal?')) return;

        try {
            await deleteMeal(mealId);
            loadStats(); // Refresh stats after deletion
        } catch (err: any) {
            alert('Failed to delete meal');
            console.error('Error deleting meal:', err);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <div className="text-center">
                    <svg className="animate-spin h-12 w-12 text-emerald-500 mx-auto mb-4" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    <p className="text-neutral-400">Loading your stats...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="text-center py-12">
                <div className="text-red-400 mb-4">{error}</div>
                <button
                    onClick={loadStats}
                    className="px-6 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg transition-colors"
                >
                    Retry
                </button>
            </div>
        );
    }

    return (
        <div className="w-full max-w-6xl mx-auto space-y-8 animate-fadeIn">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-3xl font-bold text-white">Today's Progress</h2>
                    <p className="text-neutral-400 mt-1">
                        {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                    </p>
                </div>
                <button
                    onClick={() => setShowUploadModal(true)}
                    className="px-6 py-3 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-semibold rounded-lg transition-all duration-300 transform hover:scale-105 shadow-lg shadow-emerald-500/25 flex items-center gap-2"
                >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                    Add Meal
                </button>
            </div>

            {/* Daily Stats */}
            {stats && <DailyStats stats={stats} />}

            {/* Meals Section */}
            <div>
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-xl font-semibold text-white">Today's Meals</h3>
                    <span className="text-sm text-neutral-500">
                        {stats?.totalMeals || 0} meal{stats?.totalMeals !== 1 ? 's' : ''}
                    </span>
                </div>

                {stats?.meals && stats.meals.length > 0 ? (
                    <div className="space-y-3">
                        {stats.meals.map((meal) => (
                            <MealCard key={meal.id} meal={meal} onDelete={handleDeleteMeal} />
                        ))}
                    </div>
                ) : (
                    <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-2xl p-12 text-center">
                        <svg className="w-16 h-16 text-neutral-600 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <h4 className="text-lg font-medium text-neutral-300 mb-2">No meals logged yet</h4>
                        <p className="text-neutral-500 mb-6">Start tracking your nutrition by adding your first meal!</p>
                        <button
                            onClick={() => setShowUploadModal(true)}
                            className="px-6 py-3 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold rounded-lg transition-colors"
                        >
                            Add Your First Meal
                        </button>
                    </div>
                )}
            </div>

            {/* Upload Modal */}
            <MealUploadModal
                isOpen={showUploadModal}
                onClose={() => setShowUploadModal(false)}
                onSuccess={handleMealUploadSuccess}
            />
        </div>
    );
};
