import React from 'react';

export const LoadingSettlementSkeleton: React.FC = () => {
  return (
    <div className="space-y-6 animate-pulse">
      {/* Summary Skeleton */}
      <div className="h-32 bg-slate-100 dark:bg-slate-800 rounded-2xl" />
      {/* Tabs Filter Skeleton */}
      <div className="flex gap-2 pb-2">
        <div className="h-8 w-16 bg-slate-100 dark:bg-slate-800 rounded-lg" />
        <div className="h-8 w-24 bg-slate-100 dark:bg-slate-800 rounded-lg" />
        <div className="h-8 w-24 bg-slate-100 dark:bg-slate-800 rounded-lg" />
      </div>
      {/* Row List Skeletons */}
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div
            key={i}
            className="border border-slate-200 dark:border-slate-850 rounded-2xl p-5 flex items-center justify-between gap-4"
          >
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <div className="w-10 h-10 rounded-xl bg-slate-100 dark:bg-slate-800" />
                <div className="h-4 w-4 bg-slate-100 dark:bg-slate-800 rounded" />
                <div className="w-10 h-10 rounded-xl bg-slate-100 dark:bg-slate-800" />
              </div>
              <div className="space-y-2">
                <div className="h-4 w-48 bg-slate-100 dark:bg-slate-800 rounded" />
                <div className="h-3.5 w-32 bg-slate-100 dark:bg-slate-800 rounded" />
              </div>
            </div>
            <div className="h-8 w-24 bg-slate-100 dark:bg-slate-800 rounded-lg" />
          </div>
        ))}
      </div>
    </div>
  );
};
