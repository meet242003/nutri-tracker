import { type MealSummary } from '../services/meal.service';

interface MealCardProps {
    meal: MealSummary;
    onDelete?: (mealId: string) => void;
}

export const MealCard = ({ meal, onDelete }: MealCardProps) => {
    const formatTime = (timestamp: string) => {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    };

    return (
        <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-xl p-4 hover:border-emerald-500/50 transition-all duration-300">
            <div className="flex gap-4">
                {/* Meal Image */}
                <div className="flex-shrink-0">
                    <img
                        src={meal.imageUrl || '/placeholder-meal.jpg'}
                        alt="Meal"
                        className="w-20 h-20 rounded-lg object-cover"
                    />
                </div>

                {/* Meal Info */}
                <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-start mb-2">
                        <div>
                            <p className="text-sm text-neutral-400">{formatTime(meal.uploadedAt)}</p>
                            <div className="flex flex-wrap gap-1 mt-1">
                                {meal.foodItems.slice(0, 2).map((item, idx) => (
                                    <span key={idx} className="text-xs text-emerald-400">
                                        {item}
                                    </span>
                                ))}
                                {meal.foodItems.length > 2 && (
                                    <span className="text-xs text-neutral-500">
                                        +{meal.foodItems.length - 2} more
                                    </span>
                                )}
                            </div>
                        </div>
                        {onDelete && (
                            <button
                                onClick={() => onDelete(meal.id)}
                                className="text-neutral-500 hover:text-red-400 transition-colors"
                            >
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                </svg>
                            </button>
                        )}
                    </div>

                    {/* Nutrition Summary */}
                    <div className="grid grid-cols-4 gap-2 text-center">
                        <div>
                            <div className="text-lg font-bold text-white">{Math.round(meal.nutrition.calories)}</div>
                            <div className="text-xs text-neutral-500">kcal</div>
                        </div>
                        <div>
                            <div className="text-lg font-bold text-blue-400">{Math.round(meal.nutrition.protein)}g</div>
                            <div className="text-xs text-neutral-500">Protein</div>
                        </div>
                        <div>
                            <div className="text-lg font-bold text-yellow-400">{Math.round(meal.nutrition.carbohydrates)}g</div>
                            <div className="text-xs text-neutral-500">Carbs</div>
                        </div>
                        <div>
                            <div className="text-lg font-bold text-red-400">{Math.round(meal.nutrition.fat)}g</div>
                            <div className="text-xs text-neutral-500">Fat</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
