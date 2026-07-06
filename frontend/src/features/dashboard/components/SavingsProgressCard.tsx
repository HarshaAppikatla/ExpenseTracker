import React from 'react';
import { Link } from 'react-router-dom';
import { PiggyBank, ArrowRight } from 'lucide-react';
import { SavingsGoalProgress } from '../services/dashboardService';

interface SavingsProgressCardProps {
  savingsGoalsProgress: SavingsGoalProgress[];
  currencySymbol: string;
}

export const SavingsProgressCard: React.FC<SavingsProgressCardProps> = ({
  savingsGoalsProgress,
  currencySymbol,
}) => {
  // If there are no goals, render a graceful empty state
  if (!savingsGoalsProgress || savingsGoalsProgress.length === 0) {
    return (
      <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[196px]">
        <div>
          <h2 className="text-[14px] font-bold text-slate-500 dark:text-slate-400 flex items-center gap-[6px]">
            <PiggyBank className="w-[16px] h-[16px] text-slate-400" />
            Savings Progress
          </h2>
          <div className="flex flex-col items-center justify-center py-[20px] text-center">
            <span className="text-[12px] font-medium text-slate-400 dark:text-slate-500">No active savings goals found.</span>
          </div>
        </div>
        <div className="flex justify-end pt-[8px]">
          <Link
            to="/savings"
            className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-all hover:translate-x-[2px] duration-150"
          >
            Create Goal
            <ArrowRight className="w-[14px] h-[14px]" />
          </Link>
        </div>
      </div>
    );
  }

  // Display the first savings goal
  const goal = savingsGoalsProgress[0];
  const progressPct = Math.min(Math.round(goal.progress), 100);

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[196px]">
      <div>
        {/* Header */}
        <div className="flex justify-between items-start w-full">
          <div>
            <h2 className="text-[14px] font-bold text-slate-500 dark:text-slate-400 flex items-center gap-[6px]">
              <PiggyBank className="w-[16px] h-[16px] text-slate-400" />
              Savings Progress
            </h2>
            <p className="text-[11px] text-slate-400 mt-[2px]">You are doing great! Keep it up.</p>
          </div>
        </div>

        {/* Content Layout */}
        <div className="grid grid-cols-2 gap-[12px] items-end mt-[16px]">
          {/* Percentage (Left) */}
          <div className="flex flex-col leading-none">
            <span className="text-[32px] font-extrabold text-slate-900 dark:text-slate-50 tracking-tight leading-none">
              {progressPct}%
            </span>
            <span className="text-[9px] text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider mt-[4px]">
              of goal
            </span>
          </div>

          {/* Amount details (Right) */}
          <div className="text-right pb-[4px]">
            <span className="text-[13px] font-bold text-slate-800 dark:text-slate-350">
              {currencySymbol}{goal.currentAmount.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
            </span>
            <span className="text-[11px] text-slate-400 dark:text-slate-500 block">
              of {currencySymbol}{goal.targetAmount.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
            </span>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="h-[8px] bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden w-full mt-[12px]">
          <div
            className="bg-blue-600 h-full rounded-full transition-all duration-500"
            style={{ width: `${progressPct}%` }}
          />
        </div>
      </div>

      {/* Footer link */}
      <div className="flex justify-end pt-[8px]">
        <Link
          to="/savings"
          className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-all hover:translate-x-[2px] duration-150"
        >
          View goals
          <ArrowRight className="w-[14px] h-[14px]" />
        </Link>
      </div>
    </div>
  );
};

export default SavingsProgressCard;
