import React from 'react';
import { Link } from 'react-router-dom';
import { Target, ArrowRight } from 'lucide-react';

interface BudgetStatusCardProps {
  budgetUtilization: number;
  budgetLimitTotal: number;
  budgetSpentTotal: number;
  currencySymbol: string;
}

export const BudgetStatusCard: React.FC<BudgetStatusCardProps> = ({
  budgetUtilization,
  budgetLimitTotal,
  budgetSpentTotal,
  currencySymbol,
}) => {
  const pct = Math.min(Math.round(budgetUtilization), 100);

  // Determine progress ring color
  const getProgressColor = () => {
    if (pct >= 100) return '#ef4444'; // Red
    if (pct >= 80) return '#f59e0b'; // Amber
    return '#10b981'; // Emerald
  };

  const progressColor = getProgressColor();

  // Circular gauge calculations
  const radius = 38;
  const stroke = 7;
  const normalizedRadius = radius - stroke * 2;
  const circumference = normalizedRadius * 2 * Math.PI;
  const strokeDashoffset = circumference - (pct / 100) * circumference;

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[196px]">
      <div>
        {/* Header */}
        <h2 className="text-[14px] font-bold text-slate-500 dark:text-slate-400 flex items-center gap-[6px]">
          <Target className="w-[16px] h-[16px] text-slate-400" />
          Budget Status
        </h2>

        {/* Content Layout */}
        <div className="flex items-center justify-between gap-[16px] mt-[16px]">
          {/* Circular Meter (Left) */}
          <div className="relative flex items-center justify-center w-[80px] h-[80px] shrink-0">
            <svg height="80" width="80" className="transform -rotate-90">
              <circle
                stroke="#F1F5F9"
                fill="transparent"
                strokeWidth={stroke}
                r={normalizedRadius}
                cx="40"
                cy="40"
                className="dark:stroke-slate-800"
              />
              <circle
                stroke={progressColor}
                fill="transparent"
                strokeWidth={stroke}
                strokeDasharray={circumference + ' ' + circumference}
                style={{ strokeDashoffset }}
                strokeLinecap="round"
                r={normalizedRadius}
                cx="40"
                cy="40"
                className="transition-all duration-500 ease-out"
              />
            </svg>
            <div className="absolute flex flex-col items-center justify-center">
              <span className="text-[18px] font-extrabold text-slate-900 dark:text-slate-50 leading-none">
                {pct}%
              </span>
              <span className="text-[7px] text-slate-400 dark:text-slate-500 font-bold uppercase mt-[2px] tracking-wide text-center max-w-[50px] leading-none">
                used
              </span>
            </div>
          </div>

          {/* Details (Right) */}
          <div className="text-[13px] font-medium text-slate-500 dark:text-slate-400 leading-normal">
            {budgetLimitTotal > 0 ? (
              <>
                You have used <span className="font-bold text-slate-900 dark:text-slate-200">{currencySymbol}{budgetSpentTotal.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}</span> of <span className="font-bold text-slate-900 dark:text-slate-200">{currencySymbol}{budgetLimitTotal.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}</span> budget.
              </>
            ) : (
              "No active budget limits configured for the current billing cycle."
            )}
          </div>
        </div>
      </div>

      {/* Footer link */}
      <div className="flex justify-end pt-[8px]">
        <Link
          to="/budget"
          className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-all hover:translate-x-[2px] duration-150"
        >
          View budget
          <ArrowRight className="w-[14px] h-[14px]" />
        </Link>
      </div>
    </div>
  );
};

export default BudgetStatusCard;
