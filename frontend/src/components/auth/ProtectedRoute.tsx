import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthContext } from '@/hooks/useAuthContext';
import { LoadingSpinner } from '@/components/feedback/LoadingSpinner';
import { useProfile } from '@/features/profile/hooks/useProfile';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuthContext();
  const location = useLocation();

  // Load profile only if authenticated
  const { data: profile, isLoading: isProfileLoading, error } = useProfile(isAuthenticated);

  if (isLoading || (isAuthenticated && isProfileLoading)) {
    return (
      <div className="flex h-screen w-screen items-center justify-center bg-slate-50 dark:bg-slate-950">
        <div className="flex flex-col items-center gap-4">
          <LoadingSpinner />
          <p className="text-sm font-medium text-slate-500 dark:text-slate-400 animate-pulse">
            Loading your workspace...
          </p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const isNotOnboarded = error && (error as any).raw?.response?.data?.code === 'PROF_001';

  // Redirect to onboarding if not onboarded and not currently on /onboarding
  if (isNotOnboarded && location.pathname !== '/onboarding') {
    return <Navigate to="/onboarding" replace />;
  }

  // Redirect to dashboard if already onboarded and trying to access /onboarding
  if (profile?.onboardingCompleted && location.pathname === '/onboarding') {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};
