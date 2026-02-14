
export const LandingPage = () => {
    return (
        <div className="max-w-4xl w-full text-center space-y-12 relative z-10">
            {/* Hero Section */}
            <div className="space-y-6">
                <div className="inline-block mb-4">
                    <span className="px-4 py-2 bg-emerald-500/10 border border-emerald-500/20 rounded-full text-emerald-400 text-sm font-medium backdrop-blur-sm">
                        🥗 Your Health, Simplified
                    </span>
                </div>

                <h1 className="text-6xl md:text-8xl font-bold tracking-tighter">
                    <span className="bg-gradient-to-r from-emerald-400 via-teal-400 to-cyan-400 bg-clip-text text-transparent animate-gradient">
                        Track Your Nutrition
                    </span>
                </h1>

                <p className="mt-6 text-xl md:text-2xl text-neutral-300 max-w-2xl mx-auto leading-relaxed font-light">
                    Your personal companion for mindful eating and healthier living.
                    <span className="text-emerald-400 font-medium"> Track, analyze, and improve</span> your nutrition effortlessly.
                </p>
            </div>

            {/* Features Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-12 px-4">
                <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-2xl p-6 hover:border-emerald-500/50 transition-all duration-300 hover:transform hover:scale-105">
                    <div className="text-4xl mb-3">📊</div>
                    <h3 className="text-lg font-semibold text-white mb-2">Smart Analytics</h3>
                    <p className="text-neutral-400 text-sm">Track your nutrition with AI-powered insights</p>
                </div>

                <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-2xl p-6 hover:border-emerald-500/50 transition-all duration-300 hover:transform hover:scale-105">
                    <div className="text-4xl mb-3">🎯</div>
                    <h3 className="text-lg font-semibold text-white mb-2">Goal Setting</h3>
                    <p className="text-neutral-400 text-sm">Set and achieve your health goals</p>
                </div>

                <div className="bg-neutral-800/50 backdrop-blur-sm border border-neutral-700/50 rounded-2xl p-6 hover:border-emerald-500/50 transition-all duration-300 hover:transform hover:scale-105">
                    <div className="text-4xl mb-3">📱</div>
                    <h3 className="text-lg font-semibold text-white mb-2">Easy Tracking</h3>
                    <p className="text-neutral-400 text-sm">Log meals with just a photo</p>
                </div>
            </div>

            {/* Footer */}
            <div className="pt-16 text-sm text-neutral-500">
                <p>&copy; {new Date().getFullYear()} NutriTracker. All rights reserved.</p>
            </div>
        </div>
    );
};
