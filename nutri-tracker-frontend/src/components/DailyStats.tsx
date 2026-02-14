import { type DailyStatsResponse } from '../services/meal.service';

interface DailyStatsProps {
    stats: DailyStatsResponse;
}

export const DailyStats = ({ stats }: DailyStatsProps) => {
    const calculatePercentage = (consumed: number, goal: number) => {
        if (!goal) return 0;
        return Math.min((consumed / goal) * 100, 100);
    };

    const getProgressColor = (percentage: number) => {
        if (percentage >= 90 && percentage <= 110) return 'bg-emerald-500';
        if (percentage > 110) return 'bg-red-500';
        return 'bg-blue-500';
    };

    const caloriePercentage = calculatePercentage(stats.consumed.calories, stats.goals.calories);
    const proteinPercentage = calculatePercentage(stats.consumed.protein, stats.goals.protein);
    const carbsPercentage = calculatePercentage(stats.consumed.carbohydrates, stats.goals.carbohydrates);
    const fatPercentage = calculatePercentage(stats.consumed.fat, stats.goals.fat);

    return (
        <div className="space-y-6">
            {/* Calories Card */}
            <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-2xl p-6">
                <div className="flex justify-between items-start mb-4">
                    <div>
                        <h3 className="text-lg font-semibold text-white mb-1">Calories</h3>
                        <p className="text-sm text-neutral-400">
                            {Math.round(stats.consumed.calories)} / {Math.round(stats.goals.calories)} kcal
                        </p>
                    </div>
                    <div className="text-right">
                        <div className="text-2xl font-bold text-emerald-400">
                            {Math.round(stats.remaining.calories)}
                        </div>
                        <div className="text-xs text-neutral-500">remaining</div>
                    </div>
                </div>

                {/* Progress Bar */}
                <div className="w-full bg-neutral-700 rounded-full h-3 overflow-hidden">
                    <div
                        className={`h-full transition-all duration-500 ${getProgressColor(caloriePercentage)}`}
                        style={{ width: `${caloriePercentage}%` }}
                    />
                </div>
                <p className="text-xs text-neutral-500 mt-2 text-right">{Math.round(caloriePercentage)}%</p>
            </div>

            {/* Macros Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {/* Protein */}
                <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-xl p-4">
                    <div className="flex items-center justify-between mb-3">
                        <h4 className="text-sm font-medium text-neutral-300">Protein</h4>
                        <span className="text-xs text-neutral-500">{Math.round(proteinPercentage)}%</span>
                    </div>
                    <div className="mb-2">
                        <div className="text-2xl font-bold text-blue-400">
                            {Math.round(stats.consumed.protein)}g
                        </div>
                        <div className="text-xs text-neutral-500">of {Math.round(stats.goals.protein)}g</div>
                    </div>
                    <div className="w-full bg-neutral-700 rounded-full h-2 overflow-hidden">
                        <div
                            className="h-full bg-blue-500 transition-all duration-500"
                            style={{ width: `${proteinPercentage}%` }}
                        />
                    </div>
                </div>

                {/* Carbs */}
                <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-xl p-4">
                    <div className="flex items-center justify-between mb-3">
                        <h4 className="text-sm font-medium text-neutral-300">Carbs</h4>
                        <span className="text-xs text-neutral-500">{Math.round(carbsPercentage)}%</span>
                    </div>
                    <div className="mb-2">
                        <div className="text-2xl font-bold text-yellow-400">
                            {Math.round(stats.consumed.carbohydrates)}g
                        </div>
                        <div className="text-xs text-neutral-500">of {Math.round(stats.goals.carbohydrates)}g</div>
                    </div>
                    <div className="w-full bg-neutral-700 rounded-full h-2 overflow-hidden">
                        <div
                            className="h-full bg-yellow-500 transition-all duration-500"
                            style={{ width: `${carbsPercentage}%` }}
                        />
                    </div>
                </div>

                {/* Fat */}
                <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-xl p-4">
                    <div className="flex items-center justify-between mb-3">
                        <h4 className="text-sm font-medium text-neutral-300">Fat</h4>
                        <span className="text-xs text-neutral-500">{Math.round(fatPercentage)}%</span>
                    </div>
                    <div className="mb-2">
                        <div className="text-2xl font-bold text-red-400">
                            {Math.round(stats.consumed.fat)}g
                        </div>
                        <div className="text-xs text-neutral-500">of {Math.round(stats.goals.fat)}g</div>
                    </div>
                    <div className="w-full bg-neutral-700 rounded-full h-2 overflow-hidden">
                        <div
                            className="h-full bg-red-500 transition-all duration-500"
                            style={{ width: `${fatPercentage}%` }}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};
