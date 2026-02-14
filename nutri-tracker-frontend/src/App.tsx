import { useState, useEffect } from 'react';
import { login, register, getCurrentUser, logout, type AuthResponse } from './services/auth.service';
import { getUserProfile, updateUserProfile, type UserProfileResponse, type UserProfileRequest } from './services/user.service';
import { AxiosError } from 'axios';
import { LandingPage } from './components/LandingPage';
import { OnboardingForm } from './components/OnboardingForm';
import { Dashboard } from './components/Dashboard';

function App() {
  const [showRegisterModal, setShowRegisterModal] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfileResponse | null>(null);
  const [needsOnboarding, setNeedsOnboarding] = useState(false);

  // Main loading state for initial app load
  const [appLoading, setAppLoading] = useState(true);

  // Form states
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    const initApp = async () => {
      const currentUser = getCurrentUser();
      if (currentUser) {
        setUser(currentUser);
        try {
          const profile = await getUserProfile();
          setUserProfile(profile);
          // Check if key profile fields are missing
          if (!profile.height || !profile.weight || !profile.gender || !profile.activityLevel || !profile.goal) {
            setNeedsOnboarding(true);
          }
        } catch (err) {
          console.error("Failed to load user profile", err);
        }
      }
      setAppLoading(false);
    };

    initApp();
  }, []);

  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => setFormData({ ...formData, name: e.target.value });
  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => setFormData({ ...formData, email: e.target.value });
  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => setFormData({ ...formData, password: e.target.value });

  const resetForm = () => {
    setFormData({ name: '', email: '', password: '' });
    setError(null);
    setSuccessMessage(null);
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const response = await login({ email: formData.email, password: formData.password });
      setUser(response);
      setShowLoginModal(false);
      resetForm();

      // Fetch profile after login to check onboarding status
      const profile = await getUserProfile();
      setUserProfile(profile);
      if (!profile.height || !profile.weight || !profile.gender) {
        setNeedsOnboarding(true);
      } else {
        setNeedsOnboarding(false);
      }

    } catch (err: unknown) {
      const error = err as AxiosError<{ message: string }>;
      setError(error.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccessMessage(null);
    try {
      await register({ name: formData.name, email: formData.email, password: formData.password });
      setSuccessMessage('Registration successful! Please check your email to verify your account before logging in.');
      setTimeout(() => {
        setShowRegisterModal(false);
        setShowLoginModal(true);
        resetForm();
        setSuccessMessage('Please log in with your verified account.');
      }, 3000);
    } catch (err: unknown) {
      const error = err as AxiosError<{ message: string }>;
      setError(error.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    setUser(null);
    setUserProfile(null);
    setNeedsOnboarding(false);
  };

  const openLogin = () => {
    resetForm();
    setShowRegisterModal(false);
    setShowLoginModal(true);
  };

  const openRegister = () => {
    resetForm();
    setShowLoginModal(false);
    setShowRegisterModal(true);
  };

  const handleOnboardingSubmit = async (data: UserProfileRequest) => {
    try {
      const updatedProfile = await updateUserProfile(data);
      setUserProfile(updatedProfile);
      setNeedsOnboarding(false);
    } catch (err) {
      console.error("Failed to update profile", err);
    }
  };

  if (appLoading) {
    return <div className="min-h-screen bg-neutral-950 flex items-center justify-center text-emerald-500">Loading...</div>;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-neutral-950 via-neutral-900 to-emerald-950 text-white font-sans antialiased relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl animate-pulse"></div>
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-teal-500/10 rounded-full blur-3xl animate-pulse delay-1000"></div>
      </div>

      {/* Navbar */}
      <nav className="relative z-20 border-b border-neutral-800/50 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex items-center">
              <h1 className="text-2xl font-bold bg-gradient-to-r from-emerald-400 to-teal-500 bg-clip-text text-transparent cursor-pointer" onClick={() => window.location.reload()}>
                NutriTracker
              </h1>
            </div>

            {/* CTA Buttons in Navbar */}
            <div className="flex items-center gap-3">
              {user ? (
                <div className="flex items-center gap-4">
                  <span className="text-neutral-300 hidden sm:inline">Welcome, <span className="text-emerald-400 font-medium">{user.name}</span></span>
                  <button
                    className="px-4 py-2 rounded-lg border border-neutral-700 hover:bg-neutral-800 text-neutral-300 transition-all duration-300 text-sm"
                    onClick={handleLogout}
                  >
                    Log Out
                  </button>
                </div>
              ) : (
                <>
                  <button
                    className="px-4 py-2 rounded-lg text-neutral-300 hover:text-white font-medium transition-all duration-300 hover:bg-neutral-800/50 text-sm"
                    onClick={openLogin}
                  >
                    Log In
                  </button>

                  <button
                    className="px-4 py-2 rounded-lg bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-semibold transition-all duration-300 transform hover:scale-105 shadow-lg shadow-emerald-500/25 hover:shadow-emerald-500/40 text-sm"
                    onClick={openRegister}
                  >
                    Get Started
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="flex flex-col items-center justify-center p-4 min-h-[calc(100vh-4rem)]">
        {user ? (
          needsOnboarding ? (
            <OnboardingForm onSubmit={handleOnboardingSubmit} />
          ) : (
            <Dashboard />
          )
        ) : (
          <LandingPage />
        )}
      </div>

      {/* Register Modal */}
      {showRegisterModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-fadeIn">
          <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-8 max-w-md w-full shadow-2xl transform transition-all animate-slideUp">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-white">Create Account</h2>
              <button
                onClick={() => setShowRegisterModal(false)}
                className="text-neutral-400 hover:text-white transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {error && <div className="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm">{error}</div>}
            {successMessage && <div className="mb-4 p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-400 text-sm">{successMessage}</div>}

            <form className="space-y-4" onSubmit={handleRegister}>
              <div>
                <label className="block text-sm font-medium text-neutral-300 mb-2">Full Name</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={handleNameChange}
                  className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white placeholder-neutral-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                  placeholder="John Doe"
                  required
                  minLength={3}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-neutral-300 mb-2">Email</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={handleEmailChange}
                  className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white placeholder-neutral-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                  placeholder="you@example.com"
                  required
                  minLength={5}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-neutral-300 mb-2">Password</label>
                <input
                  type="password"
                  value={formData.password}
                  onChange={handlePasswordChange}
                  className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white placeholder-neutral-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                  placeholder="••••••••"
                  required
                  minLength={8}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full px-6 py-3 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-semibold rounded-lg transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-emerald-500/25 mt-6 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Creating Account...' : 'Create Account'}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-neutral-400">
              Already have an account?{' '}
              <button
                onClick={openLogin}
                className="text-emerald-400 hover:text-emerald-300 font-medium transition-colors"
              >
                Log in
              </button>
            </p>
          </div>
        </div>
      )}

      {/* Login Modal */}
      {showLoginModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-fadeIn">
          <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-8 max-w-md w-full shadow-2xl transform transition-all animate-slideUp">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-white">Welcome Back</h2>
              <button
                onClick={() => setShowLoginModal(false)}
                className="text-neutral-400 hover:text-white transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {error && <div className="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm">{error}</div>}
            {successMessage && <div className="mb-4 p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-400 text-sm">{successMessage}</div>}

            <form className="space-y-4" onSubmit={handleLogin}>
              <div>
                <label className="block text-sm font-medium text-neutral-300 mb-2">Email</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={handleEmailChange}
                  className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white placeholder-neutral-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                  placeholder="you@example.com"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-neutral-300 mb-2">Password</label>
                <input
                  type="password"
                  value={formData.password}
                  onChange={handlePasswordChange}
                  className="w-full px-4 py-3 bg-neutral-800 border border-neutral-700 rounded-lg text-white placeholder-neutral-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                  placeholder="••••••••"
                  required
                />
              </div>

              <div className="flex items-center justify-between">
                <label className="flex items-center">
                  <input type="checkbox" className="w-4 h-4 rounded border-neutral-700 bg-neutral-800 text-emerald-500 focus:ring-emerald-500 focus:ring-offset-0" />
                  <span className="ml-2 text-sm text-neutral-400">Remember me</span>
                </label>
                <button type="button" className="text-sm text-emerald-400 hover:text-emerald-300 transition-colors">
                  Forgot password?
                </button>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full px-6 py-3 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white font-semibold rounded-lg transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-emerald-500/25 mt-6 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Logging In...' : 'Log In'}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-neutral-400">
              Don't have an account?{' '}
              <button
                onClick={openRegister}
                className="text-emerald-400 hover:text-emerald-300 font-medium transition-colors"
              >
                Sign up
              </button>
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
