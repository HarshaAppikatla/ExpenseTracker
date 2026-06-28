import React from 'react';

export const LoadingSpinner: React.FC = () => {
  return (
    <div className="flex flex-col items-center justify-center p-32 gap-16 min-h-[200px]">
      <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
      <p className="text-sm text-light-textSecondary dark:text-slate-400 font-medium">
        Connecting to ExpenseFlow services...
      </p>
    </div>
  );
};
