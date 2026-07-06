import React from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle2, AlertCircle, ArrowRight, HelpCircle } from 'lucide-react';

interface HealthScoreCardProps {
  score: number;
  budgetUtilization: number;
  savingsRate: number;
}

export const HealthScoreCard: React.FC<HealthScoreCardProps> = ({
  score,
  budgetUtilization,
  savingsRate,
}) => {
  // Determine badge color and label based on score
  const getBadgeDetails = () => {
    if (score >= 80) {
      return { text: 'Excellent', bg: 'bg-[#eefcf3] text-[#22c55e] border-[#d1fad7]', ringColor: '#22c55e' };
    } else if (score >= 60) {
      return { text: 'Good', bg: 'bg-blue-50 text-blue-600 border-blue-100', ringColor: '#3b82f6' };
    } else {
      return { text: 'Attention', bg: 'bg-amber-50 text-amber-600 border-amber-100', ringColor: '#f59e0b' };
    }
  };

  const badge = getBadgeDetails();

  // Circular gauge settings
  const radius = 36;
  const stroke = 8;
  const normalizedRadius = radius - stroke * 2;
  const circumference = normalizedRadius * 2 * Math.PI;
  const strokeDashoffset = circumference - (Math.min(score, 100) / 100) * circumference;

  // Checklist items
  const items = [
    {
      label: 'Spend within budget',
      status: budgetUtilization < 100 ? 'success' : 'danger',
    },
    {
      label: 'Maintain savings',
      status: savingsRate >= 10 ? 'success' : 'warning',
    },
    {
      label: 'Low debt ratio',
      status: 'warning', // Low debt ratio has orange warning indicator in Image 2
    },
  ];

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[280px]">
      {/* Top row */}
      <div>
        <div className="flex justify-between items-start w-full">
          <div>
            <h2 className="text-[14px] font-bold text-slate-500 dark:text-slate-400 flex items-center gap-[4px]">
              Financial Health Score
              <HelpCircle className="w-[14px] h-[14px] text-slate-350 cursor-pointer" />
            </h2>
            <p className="text-[11px] text-slate-400 mt-[2px]">Based on budget limits and savings ratios</p>
          </div>
          <span className={`text-[11px] font-bold px-[10px] py-[3px] rounded-full border ${badge.bg} capitalize`}>
            {badge.text}
          </span>
        </div>

        {/* Circular Progress & Checklist Grid */}
        <div className="grid grid-cols-2 gap-[16px] items-center mt-[24px]">
          {/* Circular Gauge */}
          <div className="relative flex items-center justify-center w-[100px] h-[100px] mx-auto">
            <svg height="100" width="100" className="transform -rotate-90">
              <circle
                stroke="#F1F5F9"
                fill="transparent"
                strokeWidth={stroke}
                r={normalizedRadius}
                cx="50"
                cy="50"
                className="dark:stroke-slate-800"
              />
              <circle
                stroke={badge.ringColor}
                fill="transparent"
                strokeWidth={stroke}
                strokeDasharray={circumference + ' ' + circumference}
                style={{ strokeDashoffset }}
                strokeLinecap="round"
                r={normalizedRadius}
                cx="50"
                cy="50"
                className="transition-all duration-500 ease-out"
              />
            </svg>
            <div className="absolute flex flex-col items-center justify-center">
              <span className="text-[26px] font-extrabold text-slate-900 dark:text-slate-50 leading-none">
                {score}
              </span>
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-semibold mt-[2px]">
                /100
              </span>
            </div>
          </div>

          {/* Checklist */}
          <div className="space-y-[8px]">
            {items.map((item, idx) => (
              <div key={idx} className="flex items-center gap-[8px] text-[12px] font-semibold text-slate-700 dark:text-slate-350">
                {item.status === 'success' ? (
                  <CheckCircle2 className="w-[16px] h-[16px] text-emerald-500 shrink-0" />
                ) : item.status === 'warning' ? (
                  <AlertCircle className="w-[16px] h-[16px] text-amber-500 shrink-0" />
                ) : (
                  <AlertCircle className="w-[16px] h-[16px] text-rose-500 shrink-0" />
                )}
                <span className="truncate">{item.label}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* CTA Footer */}
      <div className="border-t border-slate-100 dark:border-slate-800/80 pt-[12px] flex justify-end">
        <Link
          to="/insights"
          className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-all hover:translate-x-[2px] duration-150"
        >
          View full report
          <ArrowRight className="w-[14px] h-[14px]" />
        </Link>
      </div>
    </div>
  );
};

export default HealthScoreCard;
