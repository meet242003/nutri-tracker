import { useState } from 'react';
import { authApi } from '../services/api';
import type { AuthResponse } from '../types';

interface AuthFormProps {
    onAuthSuccess: (user: AuthResponse) => void;
}

export default function AuthForm({ onAuthSuccess }: AuthFormProps) {
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            if (isLogin) {
                const response = await authApi.login({
                    email: formData.email,
                    password: formData.password,
                });
                onAuthSuccess(response);
            } else {
                const response = await authApi.register({
                    name: formData.name,
                    email: formData.email,
                    password: formData.password,
                });
                // After registration, show success message
                setError('Registration successful! Please check your email to verify your account, then login.');
                setIsLogin(true);
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-teal-100 px-4">
            <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
                <div className="text-center mb-8">
                    <h1 className="text-3xl font-bold text-emerald-600 mb-2">🥗 NutriTracker</h1>
                    <p className="text-gray-600">Track your nutrition with AI</p>
                </div>

                <div className="flex gap-2 mb-6">
                    <button
                        onClick={() => {
                            setIsLogin(true);
                            setError('');
                        }}
                        className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${isLogin
                                ? 'bg-emerald-600 text-white'
                                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                            }`}
                    >
                        Login
                    </button>
                    <button
                        onClick={() => {
                            setIsLogin(false);
                            setError('');
                        }}
                        className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${!isLogin
                                ? 'bg-emerald-600 text-white'
                                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                            }`}
                    >
                        Register
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    {!isLogin && (
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Name
                            </label>
                            <input
                                type="text"
                                required
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition"
                                placeholder="John Doe"
                            />
                        </div>
                    )}

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Email
                        </label>
                        <input
                            type="email"
                            required
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition"
                            placeholder="you@example.com"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Password
                        </label>
                        <input
                            type="password"
                            required
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent outline-none transition"
                            placeholder="••••••••"
                        />
                    </div>

                    {error && (
                        <div className={`p-3 rounded-lg text-sm ${error.includes('successful')
                                ? 'bg-green-50 text-green-700 border border-green-200'
                                : 'bg-red-50 text-red-700 border border-red-200'
                            }`}>
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-emerald-600 text-white py-3 rounded-lg font-medium hover:bg-emerald-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? 'Processing...' : isLogin ? 'Login' : 'Register'}
                    </button>
                </form>
            </div>
        </div>
    );
}
