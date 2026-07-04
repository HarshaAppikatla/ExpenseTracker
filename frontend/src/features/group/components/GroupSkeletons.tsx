import React from 'react';

export const GroupListSkeleton: React.FC = () => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 animate-pulse">
      {[...Array(6)].map((_, i) => (
        <div key={i} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 h-48 space-y-4">
          <div className="flex justify-between items-start">
            <div className="h-6 w-2/3 bg-slate-200 dark:bg-slate-800 rounded"></div>
            <div className="h-5 w-16 bg-slate-200 dark:bg-slate-800 rounded-full"></div>
          </div>
          <div className="h-4 w-5/6 bg-slate-100 dark:bg-slate-800 rounded"></div>
          <div className="h-4 w-1/2 bg-slate-100 dark:bg-slate-800 rounded"></div>
          <div className="flex justify-between items-center pt-2">
            <div className="h-4 w-24 bg-slate-200 dark:bg-slate-800 rounded"></div>
            <div className="h-4 w-16 bg-slate-200 dark:bg-slate-800 rounded"></div>
          </div>
        </div>
      ))}
    </div>
  );
};

export const GroupDashboardSkeleton: React.FC = () => {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 animate-pulse">
      {/* Detail / Members columns */}
      <div className="lg:col-span-2 space-y-6">
        {/* Header summary skeleton */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 h-40 space-y-4">
          <div className="h-8 w-1/3 bg-slate-200 dark:bg-slate-800 rounded"></div>
          <div className="h-4 w-3/4 bg-slate-100 dark:bg-slate-800 rounded"></div>
          <div className="h-4 w-1/2 bg-slate-100 dark:bg-slate-800 rounded"></div>
        </div>
        {/* Members panel skeleton */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 space-y-4">
          <div className="h-6 w-1/4 bg-slate-200 dark:bg-slate-800 rounded"></div>
          {[...Array(3)].map((_, i) => (
            <div key={i} className="flex justify-between items-center py-3 border-b border-slate-100 dark:border-slate-800">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-slate-200 dark:bg-slate-800 rounded-full"></div>
                <div className="space-y-2">
                  <div className="h-4 w-28 bg-slate-200 dark:bg-slate-800 rounded"></div>
                  <div className="h-3 w-40 bg-slate-100 dark:bg-slate-800 rounded"></div>
                </div>
              </div>
              <div className="h-5 w-16 bg-slate-200 dark:bg-slate-800 rounded-full"></div>
            </div>
          ))}
        </div>
      </div>

      {/* Side tools panel skeleton */}
      <div className="space-y-6">
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 h-48 space-y-4"></div>
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 h-60 space-y-4"></div>
      </div>
    </div>
  );
};

export const TimelineSkeleton: React.FC = () => {
  return (
    <div className="space-y-6 animate-pulse">
      {[...Array(4)].map((_, i) => (
        <div key={i} className="flex gap-4">
          <div className="w-8 h-8 bg-slate-200 dark:bg-slate-800 rounded-full shrink-0"></div>
          <div className="flex-1 space-y-2 pt-1.5">
            <div className="h-4 w-3/4 bg-slate-200 dark:bg-slate-800 rounded"></div>
            <div className="h-3 w-1/4 bg-slate-100 dark:bg-slate-800 rounded"></div>
          </div>
        </div>
      ))}
    </div>
  );
};
