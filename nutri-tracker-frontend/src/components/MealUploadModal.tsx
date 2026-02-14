import { useState } from 'react';
import { uploadMealImage } from '../services/meal.service';

interface FoodItem {
    name: string;
    confidence: number;
    quantity: number;
    nutrition: {
        calories: number;
        protein: number;
        carbohydrates: number;
        fat: number;
        fiber?: number;
        sugar?: number;
    };
    visualCues?: string;
    category?: string;
}

interface NutritionSummary {
    totalCalories: number;
    totalProtein: number;
    totalCarbohydrates: number;
    totalFat: number;
    totalFiber?: number;
    totalSugar?: number;
}

interface MealAnalysisResult {
    id: string;
    imageUrl: string;
    status: string;
    detectedFoods: FoodItem[];
    nutritionSummary: NutritionSummary;
    uploadedAt: string;
    analyzedAt?: string;
}

interface MealUploadModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

export const MealUploadModal = ({ isOpen, onClose, onSuccess }: MealUploadModalProps) => {
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [preview, setPreview] = useState<string | null>(null);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [analysisResult, setAnalysisResult] = useState<MealAnalysisResult | null>(null);
    const [showAnalysis, setShowAnalysis] = useState(false);
    const [analysisStatus, setAnalysisStatus] = useState<string>('');

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setSelectedFile(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setPreview(reader.result as string);
            };
            reader.readAsDataURL(file);
            setError(null);
        }
    };

    const pollMealStatus = async (mealId: string): Promise<void> => {
        const maxAttempts = 60; // Poll for up to 2 minutes (60 * 2 seconds)
        let attempts = 0;

        return new Promise((resolve, reject) => {
            const pollInterval = setInterval(async () => {
                attempts++;

                try {
                    // Fetch meal status
                    const statusResponse = await fetch(`http://localhost:8080/api/meals/${mealId}`, {
                        headers: {
                            'Authorization': `Bearer ${localStorage.getItem('token')}`
                        }
                    });

                    if (!statusResponse.ok) {
                        clearInterval(pollInterval);
                        reject(new Error('Failed to fetch meal status'));
                        return;
                    }

                    const mealData = await statusResponse.json();
                    setAnalysisStatus(mealData.status);

                    if (mealData.status === 'ANALYZED') {
                        clearInterval(pollInterval);

                        // Fetch the full analysis
                        const analysisResponse = await fetch(`http://localhost:8080/api/meals/${mealId}/analysis`, {
                            headers: {
                                'Authorization': `Bearer ${localStorage.getItem('token')}`
                            }
                        });

                        if (analysisResponse.ok) {
                            const analysis = await analysisResponse.json();
                            setAnalysisResult(analysis);
                            setShowAnalysis(true);
                            resolve();
                        } else {
                            reject(new Error('Failed to fetch analysis'));
                        }
                    } else if (mealData.status === 'FAILED') {
                        clearInterval(pollInterval);
                        reject(new Error(mealData.errorMessage || 'Analysis failed'));
                    } else if (attempts >= maxAttempts) {
                        clearInterval(pollInterval);
                        reject(new Error('Analysis timeout - taking longer than expected'));
                    }
                } catch (err) {
                    clearInterval(pollInterval);
                    reject(err);
                }
            }, 2000); // Poll every 2 seconds
        });
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            setError('Please select an image');
            return;
        }

        setUploading(true);
        setError(null);
        setAnalysisStatus('UPLOADING');

        try {
            // Upload the image
            const response = await uploadMealImage(selectedFile);
            setAnalysisStatus('PROCESSING');

            // Poll for analysis completion
            await pollMealStatus(response.id);

        } catch (err: any) {
            setError(err.message || err.response?.data?.error || 'Failed to upload and analyze meal image');
        } finally {
            setUploading(false);
        }
    };

    const handleConfirm = () => {
        console.log('Confirming meal - calling onSuccess callback');
        onSuccess();
        console.log('onSuccess called, now closing modal');
        handleClose();
    };

    const handleClose = () => {
        setSelectedFile(null);
        setPreview(null);
        setError(null);
        setAnalysisResult(null);
        setShowAnalysis(false);
        setAnalysisStatus('');
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-fadeIn">
            <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-8 max-w-2xl w-full shadow-2xl transform transition-all animate-slideUp max-h-[90vh] overflow-y-auto">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold text-white">
                        {showAnalysis ? 'Meal Analysis' : 'Upload Meal'}
                    </h2>
                    <button
                        onClick={handleClose}
                        disabled={uploading}
                        className="text-neutral-400 hover:text-white transition-colors disabled:opacity-50"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {error && (
                    <div className="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm">
                        {error}
                    </div>
                )}

                {!showAnalysis ? (
                    <div className="space-y-4">
                        {/* File Input */}
                        <div className="border-2 border-dashed border-neutral-700 rounded-lg p-8 text-center hover:border-emerald-500/50 transition-colors">
                            {preview ? (
                                <div className="space-y-4">
                                    <img
                                        src={preview}
                                        alt="Preview"
                                        className="max-h-64 mx-auto rounded-lg"
                                    />
                                    {!uploading && (
                                        <button
                                            onClick={() => {
                                                setSelectedFile(null);
                                                setPreview(null);
                                            }}
                                            className="text-sm text-neutral-400 hover:text-white transition-colors"
                                        >
                                            Change Image
                                        </button>
                                    )}
                                </div>
                            ) : (
                                <label className="cursor-pointer">
                                    <input
                                        type="file"
                                        accept="image/*"
                                        onChange={handleFileSelect}
                                        className="hidden"
                                        disabled={uploading}
                                    />
                                    <div className="space-y-2">
                                        <svg
                                            className="w-12 h-12 mx-auto text-neutral-500"
                                            fill="none"
                                            stroke="currentColor"
                                            viewBox="0 0 24 24"
                                        >
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                strokeWidth={2}
                                                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                                            />
                                        </svg>
                                        <p className="text-neutral-300">Click to upload meal image</p>
                                        <p className="text-sm text-neutral-500">PNG, JPG up to 10MB</p>
                                    </div>
                                </label>
                            )}
                        </div>

                        {/* Status Message */}
                        {uploading && (
                            <div className="p-4 bg-blue-500/10 border border-blue-500/20 rounded-lg">
                                <div className="flex items-center gap-3">
                                    <svg className="animate-spin h-5 w-5 text-blue-400" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                                    </svg>
                                    <div>
                                        <p className="text-blue-400 font-medium">
                                            {analysisStatus === 'UPLOADING' && 'Uploading image...'}
                                            {analysisStatus === 'PROCESSING' && 'Analyzing your meal with AI...'}
                                            {analysisStatus === 'UPLOADED' && 'Processing...'}
                                        </p>
                                        <p className="text-xs text-neutral-500 mt-1">This may take a few moments</p>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Upload Button */}
                        <button
                            onClick={handleUpload}
                            disabled={!selectedFile || uploading}
                            className="w-full px-6 py-3 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-semibold rounded-lg transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-emerald-500/25 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                        >
                            {uploading ? 'Analyzing...' : 'Upload & Analyze'}
                        </button>
                    </div>
                ) : (
                    <div className="space-y-6">
                        {/* Meal Image */}
                        {analysisResult?.imageUrl && (
                            <div className="rounded-lg overflow-hidden">
                                <img
                                    src={analysisResult.imageUrl}
                                    alt="Analyzed meal"
                                    className="w-full max-h-64 object-cover"
                                />
                            </div>
                        )}

                        {/* Nutrition Summary */}
                        <div className="bg-neutral-800/50 rounded-xl p-6 border border-neutral-700/50">
                            <h3 className="text-lg font-semibold text-white mb-4">Nutrition Summary</h3>
                            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                                <div>
                                    <div className="text-3xl font-bold text-emerald-400">
                                        {Math.round(analysisResult?.nutritionSummary.totalCalories || 0)}
                                    </div>
                                    <div className="text-xs text-neutral-500 mt-1">Calories</div>
                                </div>
                                <div>
                                    <div className="text-3xl font-bold text-blue-400">
                                        {Math.round(analysisResult?.nutritionSummary.totalProtein || 0)}g
                                    </div>
                                    <div className="text-xs text-neutral-500 mt-1">Protein</div>
                                </div>
                                <div>
                                    <div className="text-3xl font-bold text-yellow-400">
                                        {Math.round(analysisResult?.nutritionSummary.totalCarbohydrates || 0)}g
                                    </div>
                                    <div className="text-xs text-neutral-500 mt-1">Carbs</div>
                                </div>
                                <div>
                                    <div className="text-3xl font-bold text-red-400">
                                        {Math.round(analysisResult?.nutritionSummary.totalFat || 0)}g
                                    </div>
                                    <div className="text-xs text-neutral-500 mt-1">Fat</div>
                                </div>
                            </div>
                        </div>

                        {/* Detected Foods */}
                        {analysisResult?.detectedFoods && analysisResult.detectedFoods.length > 0 && (
                            <div className="bg-neutral-800/50 rounded-xl p-6 border border-neutral-700/50">
                                <h3 className="text-lg font-semibold text-white mb-4">Detected Foods</h3>
                                <div className="space-y-3">
                                    {analysisResult.detectedFoods.map((food, idx) => (
                                        <div key={idx} className="flex justify-between items-center p-3 bg-neutral-900/50 rounded-lg">
                                            <div className="flex-1">
                                                <div className="flex items-center gap-2">
                                                    <span className="font-medium text-white">{food.name}</span>
                                                    {food.confidence && (
                                                        <span className="text-xs text-neutral-500">
                                                            ({Math.round(food.confidence * 100)}% confident)
                                                        </span>
                                                    )}
                                                </div>
                                                <div className="text-sm text-neutral-400 mt-1">
                                                    {food.quantity}g • {Math.round(food.nutrition.calories)} kcal
                                                </div>
                                            </div>
                                            <div className="text-right text-xs text-neutral-500">
                                                <div>P: {Math.round(food.nutrition.protein)}g</div>
                                                <div>C: {Math.round(food.nutrition.carbohydrates)}g</div>
                                                <div>F: {Math.round(food.nutrition.fat)}g</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Confirm Button */}
                        <button
                            onClick={handleConfirm}
                            className="w-full px-6 py-3 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-semibold rounded-lg transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-emerald-500/25"
                        >
                            Add to Daily Log
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};
