import React, { useEffect } from 'react';
import { useHealthCheck } from '@/features/health/services/healthService';
import { StatusCard } from '@/components/feedback/StatusCard';
import { LoadingSpinner } from '@/components/feedback/LoadingSpinner';
import { ErrorCard } from '@/components/feedback/ErrorCard';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Cpu, Terminal, Compass, Plus, Sparkles } from 'lucide-react';
import toast from 'react-hot-toast';

export const DashboardPage: React.FC = () => {
  const { data, isLoading, isError, refetch, isSuccess } = useHealthCheck({
    refetchInterval: 15000, // Poll health status every 15 seconds
  });

  // Track connection states to prevent duplicate toasts during polling
  useEffect(() => {
    if (isSuccess && data?.success) {
      toast.success('Backend Connected successfully!');
    }
  }, [isSuccess, data]);

  useEffect(() => {
    if (isError) {
      toast.error('Backend Offline. Please check server connections.');
    }
  }, [isError]);

  return (
    <div className="space-y-24">
      {/* Welcome Card banner */}
      <div className="p-24 md:p-32 rounded-card bg-gradient-to-r from-blue-600 to-indigo-700 text-white relative overflow-hidden shadow-md">
        <div className="absolute right-[-5%] top-[-20%] w-64 h-64 bg-white/10 rounded-full blur-2xl"></div>
        <div className="max-w-xl space-y-12 z-10 relative">
          <div className="inline-flex items-center gap-6 px-10 py-4 bg-white/15 rounded-full text-xs font-medium">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Sprint 00 Foundation Complete</span>
          </div>
          <h2 className="text-24 md:text-32 font-bold tracking-tight">Welcome to ExpenseFlow</h2>
          <p className="text-sm text-blue-100 leading-relaxed">
            The infrastructure base is active. Use this dashboard to verify integrations between the React client and Spring Boot core.
          </p>
        </div>
      </div>

      {/* Bento Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-24">
        {/* Widget 1: System Integration card */}
        <div className="md:col-span-1">
          {isLoading && (
            <Card className="flex items-center justify-center min-h-[220px]">
              <LoadingSpinner />
            </Card>
          )}
          
          {isError && (
            <div className="space-y-16">
              <StatusCard status="offline" />
              <ErrorCard onRetry={() => refetch()} />
            </div>
          )}

          {isSuccess && data && (
            <StatusCard status="online" details={data.data} />
          )}
        </div>

        {/* Widget 2: Frontend Tech Specs */}
        <Card className="flex flex-col justify-between gap-16 md:col-span-1">
          <div>
            <div className="flex items-center gap-8 border-b border-light-border dark:border-dark-border pb-12 mb-12">
              <Cpu className="w-5 h-5 text-primary" />
              <span className="font-bold text-sm text-light-text dark:text-dark-text">Frontend Specs</span>
            </div>
            <ul className="space-y-8 text-xs text-light-textSecondary dark:text-slate-400">
              <li className="flex justify-between"><span className="font-medium">React Framework:</span> <span className="font-semibold text-light-text dark:text-dark-text">v19.0.0</span></li>
              <li className="flex justify-between"><span className="font-medium">State/Cache Manager:</span> <span className="font-semibold text-light-text dark:text-dark-text">TanStack Query v5</span></li>
              <li className="flex justify-between"><span className="font-medium">UI Component Base:</span> <span className="font-semibold text-light-text dark:text-dark-text">Material UI v6/v7</span></li>
              <li className="flex justify-between"><span className="font-medium">Styling Processor:</span> <span className="font-semibold text-light-text dark:text-dark-text">Tailwind CSS</span></li>
              <li className="flex justify-between"><span className="font-medium">Validation Core:</span> <span className="font-semibold text-light-text dark:text-dark-text">Zod & React Hook Form</span></li>
            </ul>
          </div>
          <div className="text-[10px] text-light-textSecondary dark:text-slate-500">
            Build Mode: {import.meta.env.MODE}
          </div>
        </Card>

        {/* Widget 3: Quick Action Launchpad */}
        <Card className="flex flex-col justify-between gap-16 md:col-span-1">
          <div>
            <div className="flex items-center gap-8 border-b border-light-border dark:border-dark-border pb-12 mb-12">
              <Terminal className="w-5 h-5 text-primary" />
              <span className="font-bold text-sm text-light-text dark:text-dark-text">Launchpad Actions</span>
            </div>
            <p className="text-xs text-light-textSecondary dark:text-slate-400 mb-16">
              Create and manage spaces. Buttons are currently placeholder actions for future sprints.
            </p>
            <div className="space-y-8">
              <Button variant="outlined" className="w-full justify-start text-xs cursor-not-allowed opacity-50" disabled>
                <Plus className="w-4 h-4" />
                <span>Create Shared Group</span>
              </Button>
              <Button variant="outlined" className="w-full justify-start text-xs cursor-not-allowed opacity-50" disabled>
                <Compass className="w-4 h-4" />
                <span>Create Group Trip</span>
              </Button>
            </div>
          </div>
          <div className="text-[10px] text-light-textSecondary dark:text-slate-500">
            Sprint 01 Authentication coming soon
          </div>
        </Card>
      </div>
    </div>
  );
};
