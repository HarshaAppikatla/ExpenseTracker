import React from 'react';

interface EmptySettlementStateProps {
  message?: string;
  subMessage?: string;
}

export const EmptySettlementState: React.FC<EmptySettlementStateProps> = ({
  message = 'All Clear! No Outstanding Settlements',
  subMessage = 'Everything is fully settled within the group. Add posted expenses to start tracking balances.',
}) => {
  return (
    <div className="text-center py-12 px-6 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm flex flex-col items-center justify-center">
      <div className="w-16 h-16 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-850 rounded-2xl flex items-center justify-center mb-4 text-slate-400 dark:text-slate-550 shadow-inner">
        <svg
          className="w-8 h-8"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth="1.5"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      </div>
      <h3 className="text-md font-bold text-slate-800 dark:text-slate-250 mb-1">
        {message}
      </h3>
      <p className="text-xs text-slate-550 dark:text-slate-400 max-w-sm">
        {subMessage}
      </p>
    </div>
  );
};
