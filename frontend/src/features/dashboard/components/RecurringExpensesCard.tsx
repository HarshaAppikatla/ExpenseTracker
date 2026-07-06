import React from 'react';
import { Link } from 'react-router-dom';
import { Repeat, ArrowRight, Play, Music, Cloud } from 'lucide-react';
import { RecurringTransaction } from '@/features/recurring/services/recurringService';

interface RecurringExpensesCardProps {
  recurringTransactions: RecurringTransaction[];
}

export const RecurringExpensesCard: React.FC<RecurringExpensesCardProps> = ({
  recurringTransactions,
}) => {
  // Filter active recurring subscriptions (excluding completions)
  const activeSubs = recurringTransactions.filter(t => t.status === 'ACTIVE' && t.transactionType === 'EXPENSE');
  const activeCount = activeSubs.length;

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[20px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[180px] w-full">
      <div>
        {/* Header */}
        <h2 className="text-[13px] font-bold text-slate-500 dark:text-slate-400 flex items-center gap-[6px]">
          <Repeat className="w-[15px] h-[15px] text-slate-400" />
          Recurring Expenses
        </h2>
        <p className="text-[11px] text-slate-400 mt-[2px] font-medium">
          {activeCount > 0 ? `${activeCount} active subscriptions` : 'No active subscriptions'}
        </p>

        {/* Brand Icons Row */}
        <div className="flex items-center gap-[8px] mt-[20px]">
          {/* Netflix Mock */}
          <div 
            className="w-[32px] h-[32px] rounded-full bg-black flex items-center justify-center text-[#E50914] shadow-sm shrink-0 border border-slate-900"
            title="Netflix Subscription"
          >
            <Play className="w-[14px] h-[14px] fill-current" />
          </div>

          {/* Spotify Mock */}
          <div 
            className="w-[32px] h-[32px] rounded-full bg-[#1DB954] flex items-center justify-center text-white shadow-sm shrink-0"
            title="Spotify Subscription"
          >
            <Music className="w-[14px] h-[14px]" />
          </div>

          {/* Cloud Provider Mock */}
          <div 
            className="w-[32px] h-[32px] rounded-full bg-blue-50 text-blue-600 dark:bg-blue-950/30 dark:text-blue-400 flex items-center justify-center shadow-sm shrink-0 border border-blue-100 dark:border-blue-900/40"
            title="Cloud Storage Subscription"
          >
            <Cloud className="w-[14px] h-[14px] fill-current" />
          </div>

          {/* Optional Extra Indicator Pill */}
          {activeCount > 3 && (
            <div className="w-[32px] h-[32px] rounded-full bg-slate-50 border border-slate-200 dark:bg-slate-800 dark:border-slate-700 flex items-center justify-center text-[11px] font-extrabold text-slate-500 dark:text-slate-400 shrink-0">
              +{activeCount - 3}
            </div>
          )}
        </div>
      </div>

      {/* Footer link */}
      <div className="flex justify-end pt-[8px]">
        <Link
          to="/recurring"
          className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-all hover:translate-x-[2px] duration-150"
        >
          Manage recurring
          <ArrowRight className="w-[14px] h-[14px]" />
        </Link>
      </div>
    </div>
  );
};

export default RecurringExpensesCard;
