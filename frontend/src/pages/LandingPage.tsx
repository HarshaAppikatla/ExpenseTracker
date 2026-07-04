import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Compass, Users, Sparkles, ArrowRight, ShieldCheck } from 'lucide-react';
import { APP_NAME } from '@/core/constants';
import { Button } from '@/components/ui/Button';
import { Footer } from '@/components/layout/Footer';
import { useAuthContext } from '@/hooks/useAuthContext';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthContext();

  const handleCtaClick = () => {
    if (isAuthenticated) {
      navigate('/dashboard');
    } else {
      navigate('/register');
    }
  };

  const handleSignInClick = () => {
    if (isAuthenticated) {
      navigate('/dashboard');
    } else {
      navigate('/login');
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between overflow-x-hidden relative">
      {/* Background ambient glow shapes */}
      <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-blue-900/10 rounded-full blur-[120px]"></div>
      <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-teal-900/10 rounded-full blur-[120px]"></div>

      {/* Navigation Header */}
      <header className="sticky top-0 z-50 w-full px-24 py-16 glassmorphism-dark">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-8">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs">
              EF
            </div>
            <span className="font-bold text-base text-white">{APP_NAME}</span>
          </div>

          <div className="flex items-center gap-16">
            <Button variant="text" className="text-slate-300 hover:text-white" onClick={() => navigate('/dashboard')}>
              Dashboard
            </Button>
            <Button variant="filled" className="bg-primary text-white" onClick={handleSignInClick}>
              {isAuthenticated ? 'Enter App' : 'Sign In'}
            </Button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1 max-w-5xl mx-auto px-24 py-64 md:py-96 flex flex-col items-center text-center gap-32 z-10">
        <div className="inline-flex items-center gap-8 px-12 py-6 rounded-full bg-slate-900 border border-slate-800 text-xs text-primary font-medium animate-pulse">
          <Sparkles className="w-16 h-16" />
          <span>Expense Sharing Redefined</span>
        </div>

        <h2 className="text-4xl md:text-6xl font-extrabold tracking-tight text-white max-w-3xl leading-none">
          Track, Split, and Settle Expenses <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-500 to-teal-400">Harmoniously</span>.
        </h2>

        <p className="text-lg text-slate-400 max-w-2xl leading-relaxed">
          ExpenseFlow turns group trips and shared housing into collaborative workspaces. Stop dealing with messy spreadsheets, balance math, and constant reminders.
        </p>

        <div className="flex flex-col sm:flex-row gap-16 w-full sm:w-auto">
          <Button variant="filled" className="h-12 px-24" onClick={handleCtaClick}>
            <span>{isAuthenticated ? 'Go to Dashboard' : 'Get Started Free'}</span>
            <ArrowRight className="w-16 h-16" />
          </Button>
          <Button variant="outlined" className="h-12 px-24 border-slate-700 text-slate-300 hover:bg-slate-900" onClick={() => navigate('/dashboard')}>
            <span>Explore Features</span>
          </Button>
        </div>

        {/* Feature Bento Grid */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-24 mt-64 w-full">
          <div className="glassmorphism-card-dark p-24 rounded-card flex flex-col items-start gap-12 text-left">
            <div className="p-8 bg-blue-950 border border-blue-800 text-blue-400 rounded-btn">
              <Compass className="w-16 h-16" />
            </div>
            <h3 className="font-bold text-base text-white">Trip Workspaces</h3>
            <p className="text-sm text-slate-400">
              Treat every vacation or event as an active, chronological timeline of shared payments and itineraries.
            </p>
          </div>

          <div className="glassmorphism-card-dark p-24 rounded-card flex flex-col items-start gap-12 text-left">
            <div className="p-8 bg-teal-950 border border-teal-800 text-teal-400 rounded-btn">
              <Users className="w-16 h-16" />
            </div>
            <h3 className="font-bold text-base text-white">Smart Split Engine</h3>
            <p className="text-sm text-slate-400">
              Split bills dynamically by percentages, shares, or specific item weights. Everyone pays their fair share.
            </p>
          </div>

          <div className="glassmorphism-card-dark p-24 rounded-card flex flex-col items-start gap-12 text-left">
            <div className="p-8 bg-purple-950 border border-purple-800 text-purple-400 rounded-btn">
              <ShieldCheck className="w-16 h-16" />
            </div>
            <h3 className="font-bold text-base text-white">Immutability & Safety</h3>
            <p className="text-sm text-slate-400">
              Designed with enterprise audit standards. Financial entries are never hard deleted, keeping history safe.
            </p>
          </div>
        </section>
      </main>

      {/* Footer */}
      <Footer />
    </div>
  );
};
