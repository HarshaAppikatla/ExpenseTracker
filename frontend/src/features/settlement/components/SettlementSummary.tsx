import React from 'react';
import { SettlementSummaryResponse } from '../types';

interface SettlementSummaryProps {
  summary: SettlementSummaryResponse;
  isAdminOrOwner: boolean;
  onGenerateClick: () => void;
  isGenerating: boolean;
}

const SettlementSummaryComponent: React.FC<SettlementSummaryProps> = ({
  summary,
  isAdminOrOwner,
  onGenerateClick,
  isGenerating,
}) => {
  const formatAmount = (val: number, currency: string) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency || 'INR',
    }).format(val);
  };

  return (
    <div className="bg-gradient-to-br from-slate-900 via-slate-850 to-slate-800 dark:from-slate-950 dark:to-slate-900 text-white rounded-2xl p-6 shadow-xl border border-slate-200/10 relative overflow-hidden transition-all duration-300 hover:shadow-2xl">
      {/* Background radial highlight */}
      <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-500/10 rounded-full blur-3xl -mr-20 -mt-20 pointer-events-none" />
      <div className="absolute bottom-0 left-0 w-64 h-64 bg-blue-500/5 rounded-full blur-3xl -ml-20 -mb-20 pointer-events-none" />

      <div className="relative flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <span className="text-xs font-semibold text-slate-400 uppercase tracking-widest">
            Total Outstanding Debts
          </span>
          <div className="text-3xl sm:text-4xl font-extrabold tracking-tight mt-1 flex items-baseline gap-2 bg-gradient-to-r from-white via-slate-100 to-slate-300 bg-clip-text text-transparent">
            {formatAmount(summary.totalOutstanding || 0, summary.currency)}
            <span className="text-sm font-medium text-slate-400 uppercase">
              {summary.currency}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-2">
            Simplified debt graph represents the minimum transactions needed to clear all balances.
          </p>
        </div>

        <div className="flex items-center gap-4">
          <div className="bg-slate-800/80 dark:bg-slate-900/80 border border-slate-700/50 rounded-xl px-4 py-3 text-center min-w-[100px]">
            <div className="text-2xl font-bold text-indigo-400">
              {summary.pendingCount || 0}
            </div>
            <div className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-0.5">
              Active Debts
            </div>
          </div>

          {isAdminOrOwner && (
            <button
              onClick={onGenerateClick}
              disabled={isGenerating}
              aria-label="Recalculate and simplify group settlements"
              className="relative group overflow-hidden px-5 py-3 rounded-xl bg-gradient-to-r from-indigo-500 to-blue-600 hover:from-indigo-600 hover:to-blue-700 text-sm font-semibold shadow-lg hover:shadow-indigo-500/20 active:scale-98 transition-all disabled:opacity-50 disabled:pointer-events-none"
            >
              <span className="relative flex items-center gap-2">
                {isGenerating ? (
                  <>
                    <svg
                      className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                      fill="none"
                      viewBox="0 0 24 24"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      />
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      />
                    </svg>
                    Simplifying...
                  </>
                ) : (
                  <>
                    <svg
                      className="w-4 h-4 transition-transform group-hover:rotate-180 duration-500"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                      strokeWidth="2.5"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        d="M4 4v5h.582m15.356 2A8.001 8.001 0 1121.21 7.89H18.5"
                      />
                    </svg>
                    Recalculate Debts
                  </>
                )}
              </span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export const SettlementSummary = React.memo(SettlementSummaryComponent);
