import { useState, useEffect } from 'react';
import AuthForm from './components/AuthForm';
import ProfileSetup from './components/ProfileSetup';
import Dashboard from './components/Dashboard';
import MealUpload from './components/MealUpload';
import ManualEntry from './components/ManualEntry';
import { authApi, userApi } from './services/api';
import type { AuthResponse, UserProfileResponse } from './types';

type View = 'dashboard' | 'upload' | 'history';

function App() {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [showProfileSetup, setShowProfileSetup] = useState(false);
  const [showManualEntry, setShowManualEntry] = useState(false);
  const [currentView, setCurrentView] = useState<View>('dashboard');
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const profileData = await userApi.getProfile();
        setProfile(profileData);

        // Check if profile is incomplete
        if (!profileData.height || !profileData.weight || !profileData.goal) {
          setShowProfileSetup(true);
        }
      } catch (err) {
        console.error('Failed to load profile:', err);
      }
    }
    setLoading(false);
  };

  const handleAuthSuccess = (userData: AuthResponse) => {
    setUser(userData);
    setShowProfileSetup(true);
  };

  const handleProfileComplete = async () => {
    setShowProfileSetup(false);
    try {
      const profileData = await userApi.getProfile();
      setProfile(profileData);
    } catch (err) {
      console.error('Failed to load profile:', err);
    }
  };

  const handleLogout = () => {
    authApi.logout();
    setUser(null);
    setProfile(null);
  };

  const handleMealSuccess = () => {
    setShowManualEntry(false);
    setRefreshKey(prev => prev + 1);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (!user && !localStorage.getItem('authToken')) {
    return <AuthForm onAuthSuccess={handleAuthSuccess} />;
  }

  if (showProfileSetup) {
    return <ProfileSetup onComplete={handleProfileComplete} />;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 to-teal-100">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold text-emerald-600">🥗 NutriTracker</h1>
            </div>
            <div className="flex items-center gap-4">
              {profile && (
                <div className="text-right hidden sm:block">
                  <p className="text-sm font-medium text-gray-800">{profile.name}</p>
                  <p className="text-xs text-gray-500">{profile.email}</p>
                </div>
              )}
              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg font-medium transition-colors"
              >
                Logout
              </button>
            </div>
          </div>

          {/* Navigation */}
          <nav className="mt-4 flex gap-2 border-t border-gray-200 pt-4">
            <button
              onClick={() => setCurrentView('dashboard')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${currentView === 'dashboard'
                  ? 'bg-emerald-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-50'
                }`}
            >
              📊 Dashboard
            </button>
            <button
              onClick={() => setCurrentView('upload')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${currentView === 'upload'
                  ? 'bg-emerald-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-50'
                }`}
            >
              📸 Upload Meal
            </button>
            <button
              onClick={() => setShowManualEntry(true)}
              className="px-4 py-2 bg-white text-gray-700 hover:bg-gray-50 rounded-lg font-medium transition-colors"
            >
              ✍️ Manual Entry
            </button>
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <main className="py-8">
        {currentView === 'dashboard' && <Dashboard key={refreshKey} />}
        {currentView === 'upload' && (
          <MealUpload onSuccess={() => setRefreshKey(prev => prev + 1)} />
        )}
      </main>

      {/* Manual Entry Modal */}
      {showManualEntry && (
        <ManualEntry
          onSuccess={handleMealSuccess}
          onCancel={() => setShowManualEntry(false)}
        />
      )}
    </div>
  );
}

export default App;
