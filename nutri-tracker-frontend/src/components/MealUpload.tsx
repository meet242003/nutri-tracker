import { useState, useRef } from 'react';
import { mealsApi } from '../services/api';
import type { MealImage } from '../types';

interface MealUploadProps {
    onSuccess?: () => void;
}

export default function MealUpload({ onSuccess }: MealUploadProps) {
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [preview, setPreview] = useState<string>('');
    const [uploading, setUploading] = useState(false);
    const [analyzing, setAnalyzing] = useState(false);
    const [uploadedMealId, setUploadedMealId] = useState<string>('');
    const [analysis, setAnalysis] = useState<MealImage | null>(null);
    const [error, setError] = useState('');
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setSelectedFile(file);
            setPreview(URL.createObjectURL(file));
            setError('');
            setAnalysis(null);
        }
    };

    const handleUpload = async () => {
        if (!selectedFile) return;

        setUploading(true);
        setError('');

        try {
            const response = await mealsApi.uploadMealImage(selectedFile);
            setUploadedMealId(response.id);

            // Start polling for analysis
            setAnalyzing(true);
            pollForAnalysis(response.id);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Upload failed');
        } finally {
            setUploading(false);
        }
    };

    const pollForAnalysis = async (mealId: string) => {
        let attempts = 0;
        const maxAttempts = 120; // 30 seconds max

        const poll = async () => {
            try {
                const analysisResponse = await mealsApi.getMealAnalysis(mealId);
                console.log(analysisResponse);

                if (analysisResponse.status === 'ANALYZED') {
                    setAnalysis(analysisResponse);
                    setAnalyzing(false);
                    // Call onSuccess callback to refresh dashboard
                    onSuccess?.();
                } else if (analysisResponse.status === 'FAILED') {
                    setError('Analysis failed: ' + analysisResponse.errorMessage);
                    setAnalyzing(false);
                } else if (attempts < maxAttempts) {
                    attempts++;
                    setTimeout(poll, 1000);
                } else {
                    setError('Analysis timeout. Please try again.');
                    setAnalyzing(false);
                }
            } catch (err) {
                if (attempts < maxAttempts) {
                    attempts++;
                    setTimeout(poll, 1000);
                } else {
                    setError('Failed to get analysis');
                    setAnalyzing(false);
                }
            }
        };

        poll();
    };

    const handleReset = () => {
        setSelectedFile(null);
        setPreview('');
        setAnalysis(null);
        setUploadedMealId('');
        setError('');
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    return (
        <div className="max-w-4xl mx-auto p-6">
            <div className="bg-white rounded-2xl shadow-lg p-8">
                <h2 className="text-2xl font-bold text-gray-800 mb-6">Upload Meal Image</h2>

                {/* File Upload Area */}
                <div className="mb-6">
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/*"
                        onChange={handleFileSelect}
                        className="hidden"
                        id="file-upload"
                    />
                    <label
                        htmlFor="file-upload"
                        className="flex flex-col items-center justify-center w-full h-64 border-2 border-dashed border-gray-300 rounded-lg cursor-pointer hover:border-emerald-500 transition-colors bg-gray-50"
                    >
                        {preview ? (
                            <img src={preview} alt="Preview" className="h-full object-contain rounded-lg" />
                        ) : (
                            <div className="text-center">
                                <svg className="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                                    <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                                </svg>
                                <p className="mt-2 text-sm text-gray-600">Click to upload or drag and drop</p>
                                <p className="text-xs text-gray-500">PNG, JPG, GIF up to 10MB</p>
                            </div>
                        )}
                    </label>
                </div>

                {/* Action Buttons */}
                <div className="flex gap-3 mb-6">
                    <button
                        onClick={handleUpload}
                        disabled={!selectedFile || uploading || analyzing}
                        className="flex-1 bg-emerald-600 text-white py-3 rounded-lg font-medium hover:bg-emerald-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {uploading ? 'Uploading...' : analyzing ? 'Analyzing...' : 'Upload & Analyze'}
                    </button>
                    {(selectedFile || analysis) && (
                        <button
                            onClick={handleReset}
                            className="px-6 py-3 border border-gray-300 rounded-lg font-medium hover:bg-gray-50 transition-colors"
                        >
                            Reset
                        </button>
                    )}
                </div>

                {/* Error Message */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
                        {error}
                    </div>
                )}

                {/* Analysis Results */}
                {analysis && (
                    <div className="border-t pt-6">
                        <h3 className="text-xl font-bold text-gray-800 mb-4">Nutrition Analysis</h3>

                        {/* Summary Cards */}
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
                            <div className="bg-gradient-to-br from-orange-50 to-orange-100 p-4 rounded-lg">
                                <p className="text-sm text-orange-600 font-medium">Calories</p>
                                <p className="text-2xl font-bold text-orange-700">
                                    {Math.round(analysis.nutritionSummary.totalCalories)}
                                </p>
                            </div>
                            <div className="bg-gradient-to-br from-blue-50 to-blue-100 p-4 rounded-lg">
                                <p className="text-sm text-blue-600 font-medium">Protein</p>
                                <p className="text-2xl font-bold text-blue-700">
                                    {Math.round(analysis.nutritionSummary.totalProtein)}g
                                </p>
                            </div>
                            <div className="bg-gradient-to-br from-amber-50 to-amber-100 p-4 rounded-lg">
                                <p className="text-sm text-amber-600 font-medium">Carbs</p>
                                <p className="text-2xl font-bold text-amber-700">
                                    {Math.round(analysis.nutritionSummary.totalCarbohydrates)}g
                                </p>
                            </div>
                            <div className="bg-gradient-to-br from-red-50 to-red-100 p-4 rounded-lg">
                                <p className="text-sm text-red-600 font-medium">Fat</p>
                                <p className="text-2xl font-bold text-red-700">
                                    {Math.round(analysis.nutritionSummary.totalFat)}g
                                </p>
                            </div>
                            <div className="bg-gradient-to-br from-green-50 to-green-100 p-4 rounded-lg">
                                <p className="text-sm text-green-600 font-medium">Fiber</p>
                                <p className="text-2xl font-bold text-green-700">
                                    {Math.round(analysis.nutritionSummary.totalFiber)}g
                                </p>
                            </div>
                            <div className="bg-gradient-to-br from-purple-50 to-purple-100 p-4 rounded-lg">
                                <p className="text-sm text-purple-600 font-medium">Sugar</p>
                                <p className="text-2xl font-bold text-purple-700">
                                    {Math.round(analysis.nutritionSummary.totalSugar)}g
                                </p>
                            </div>
                        </div>

                        {/* Detected Foods */}
                        <div>
                            <h4 className="font-semibold text-gray-800 mb-3">Detected Foods</h4>
                            <div className="space-y-4">
                                {analysis.detectedFoods.map((food, index) => (
                                    <div key={index} className="bg-gray-50 rounded-lg p-4">
                                        <div className="flex justify-between items-start mb-2">
                                            <div>
                                                <h5 className="font-semibold text-gray-800">{food.name}</h5>
                                                <p className="text-sm text-gray-600">{food.quantity}g • {food.category}</p>
                                            </div>
                                            <span className="text-xs bg-emerald-100 text-emerald-700 px-2 py-1 rounded">
                                                {Math.round(food.confidence * 100)}% confident
                                            </span>
                                        </div>

                                        <div className="grid grid-cols-3 gap-2 text-sm mt-3">
                                            <div>
                                                <span className="text-gray-600">Cal:</span>
                                                <span className="font-medium ml-1">{Math.round(food.nutrition.calories)}</span>
                                            </div>
                                            <div>
                                                <span className="text-gray-600">Protein:</span>
                                                <span className="font-medium ml-1">{Math.round(food.nutrition.protein)}g</span>
                                            </div>
                                            <div>
                                                <span className="text-gray-600">Carbs:</span>
                                                <span className="font-medium ml-1">{Math.round(food.nutrition.carbohydrates)}g</span>
                                            </div>
                                        </div>

                                        {food.ingredientBreakdown && food.ingredientBreakdown.length > 0 && (
                                            <details className="mt-3">
                                                <summary className="text-sm text-emerald-600 cursor-pointer hover:text-emerald-700">
                                                    View Ingredients ({food.ingredientBreakdown.length})
                                                </summary>
                                                <div className="mt-2 space-y-1 pl-4">
                                                    {food.ingredientBreakdown.map((ingredient, idx) => (
                                                        <div key={idx} className="text-sm text-gray-600">
                                                            • {ingredient.name} ({ingredient.quantityGrams}g)
                                                        </div>
                                                    ))}
                                                </div>
                                            </details>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
