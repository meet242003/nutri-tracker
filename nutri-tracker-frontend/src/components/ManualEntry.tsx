import { useState } from 'react';
import { mealsApi } from '../services/api';
import type { FoodSearchItem, ManualFoodEntry } from '../types';

interface ManualEntryProps {
    onSuccess: () => void;
    onCancel: () => void;
}

export default function ManualEntry({ onSuccess, onCancel }: ManualEntryProps) {
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<FoodSearchItem[]>([]);
    const [selectedFoods, setSelectedFoods] = useState<ManualFoodEntry[]>([]);
    const [searching, setSearching] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    const handleSearch = async () => {
        if (!searchQuery.trim()) return;

        setSearching(true);
        setError('');

        try {
            const results = await mealsApi.searchFoods(searchQuery, 10);
            setSearchResults(results.results);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Search failed');
        } finally {
            setSearching(false);
        }
    };

    const addFood = (food: FoodSearchItem, quantity: number) => {
        const factor = quantity / 100;
        const entry: ManualFoodEntry = {
            name: food.name,
            quantityGrams: quantity,
            nutrition: {
                calories: food.nutritionPer100g.calories * factor,
                protein: food.nutritionPer100g.protein * factor,
                carbohydrates: food.nutritionPer100g.carbohydrates * factor,
                fat: food.nutritionPer100g.fat * factor,
                fiber: food.nutritionPer100g.fiber * factor,
                sugar: food.nutritionPer100g.sugar * factor,
            },
        };
        setSelectedFoods([...selectedFoods, entry]);
        setSearchQuery('');
        setSearchResults([]);
    };

    const removeFood = (index: number) => {
        setSelectedFoods(selectedFoods.filter((_, i) => i !== index));
    };

    const handleSubmit = async () => {
        if (selectedFoods.length === 0) {
            setError('Please add at least one food item');
            return;
        }

        setSubmitting(true);
        setError('');

        try {
            await mealsApi.createManualEntry({ foods: selectedFoods });
            onSuccess();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to create entry');
        } finally {
            setSubmitting(false);
        }
    };

    const totalNutrition = selectedFoods.reduce(
        (acc, food) => ({
            calories: acc.calories + food.nutrition.calories,
            protein: acc.protein + food.nutrition.protein,
            carbohydrates: acc.carbohydrates + food.nutrition.carbohydrates,
            fat: acc.fat + food.nutrition.fat,
        }),
        { calories: 0, protein: 0, carbohydrates: 0, fat: 0 }
    );

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-2xl shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6 border-b border-gray-200 sticky top-0 bg-white">
                    <div className="flex items-center justify-between">
                        <h2 className="text-2xl font-bold text-gray-800">Add Manual Entry</h2>
                        <button
                            onClick={onCancel}
                            className="text-gray-500 hover:text-gray-700 text-2xl"
                        >
                            ×
                        </button>
                    </div>
                </div>

                <div className="p-6">
                    {error && (
                        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
                            {error}
                        </div>
                    )}

                    {/* Search Section */}
                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Search Food Database
                        </label>
                        <div className="flex gap-2">
                            <input
                                type="text"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                                placeholder="Search for chicken, rice, banana..."
                                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
                            />
                            <button
                                onClick={handleSearch}
                                disabled={searching}
                                className="px-6 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-lg transition-colors disabled:bg-gray-400"
                            >
                                {searching ? 'Searching...' : 'Search'}
                            </button>
                        </div>
                    </div>

                    {/* Search Results */}
                    {searchResults.length > 0 && (
                        <div className="mb-6 border border-gray-200 rounded-lg p-4 max-h-60 overflow-y-auto">
                            <h3 className="font-semibold text-gray-800 mb-3">Search Results</h3>
                            <div className="space-y-2">
                                {searchResults.map((food) => (
                                    <FoodSearchResult
                                        key={food.id}
                                        food={food}
                                        onAdd={addFood}
                                    />
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Selected Foods */}
                    <div className="mb-6">
                        <h3 className="font-semibold text-gray-800 mb-3">Selected Foods</h3>
                        {selectedFoods.length === 0 ? (
                            <p className="text-gray-500 text-center py-8 border border-dashed border-gray-300 rounded-lg">
                                No foods added yet. Search and add foods above.
                            </p>
                        ) : (
                            <div className="space-y-3">
                                {selectedFoods.map((food, index) => (
                                    <div
                                        key={index}
                                        className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                                    >
                                        <div className="flex-1">
                                            <p className="font-medium text-gray-800">{food.name}</p>
                                            <p className="text-sm text-gray-600">
                                                {food.quantityGrams}g • {Math.round(food.nutrition.calories)} kcal
                                            </p>
                                        </div>
                                        <button
                                            onClick={() => removeFood(index)}
                                            className="text-red-600 hover:text-red-700 font-medium"
                                        >
                                            Remove
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Total Nutrition */}
                    {selectedFoods.length > 0 && (
                        <div className="mb-6 bg-emerald-50 border border-emerald-200 rounded-lg p-4">
                            <h3 className="font-semibold text-gray-800 mb-3">Total Nutrition</h3>
                            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                                <div>
                                    <p className="text-sm text-gray-600">Calories</p>
                                    <p className="text-xl font-bold text-emerald-600">
                                        {Math.round(totalNutrition.calories)}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-600">Protein</p>
                                    <p className="text-xl font-bold text-blue-600">
                                        {Math.round(totalNutrition.protein)}g
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-600">Carbs</p>
                                    <p className="text-xl font-bold text-amber-600">
                                        {Math.round(totalNutrition.carbohydrates)}g
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-600">Fat</p>
                                    <p className="text-xl font-bold text-purple-600">
                                        {Math.round(totalNutrition.fat)}g
                                    </p>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Actions */}
                    <div className="flex gap-3">
                        <button
                            onClick={onCancel}
                            className="flex-1 px-6 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-lg transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            onClick={handleSubmit}
                            disabled={submitting || selectedFoods.length === 0}
                            className="flex-1 px-6 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-lg transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                        >
                            {submitting ? 'Adding...' : 'Add to Log'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

// Food Search Result Component
function FoodSearchResult({
    food,
    onAdd,
}: {
    food: FoodSearchItem;
    onAdd: (food: FoodSearchItem, quantity: number) => void;
}) {
    const [quantity, setQuantity] = useState(100);

    return (
        <div className="flex items-center justify-between p-3 bg-white border border-gray-200 rounded-lg hover:shadow-md transition-shadow">
            <div className="flex-1">
                <p className="font-medium text-gray-800">{food.name}</p>
                <p className="text-xs text-gray-500">{food.category}</p>
                <p className="text-sm text-gray-600 mt-1">
                    Per 100g: {Math.round(food.nutritionPer100g.calories)} kcal •{' '}
                    P: {Math.round(food.nutritionPer100g.protein)}g •{' '}
                    C: {Math.round(food.nutritionPer100g.carbohydrates)}g •{' '}
                    F: {Math.round(food.nutritionPer100g.fat)}g
                </p>
            </div>
            <div className="flex items-center gap-2 ml-4">
                <input
                    type="number"
                    value={quantity}
                    onChange={(e) => setQuantity(parseInt(e.target.value) || 0)}
                    className="w-20 px-2 py-1 border border-gray-300 rounded text-center"
                    min="1"
                />
                <span className="text-sm text-gray-600">g</span>
                <button
                    onClick={() => onAdd(food, quantity)}
                    className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium rounded-lg transition-colors"
                >
                    Add
                </button>
            </div>
        </div>
    );
}
