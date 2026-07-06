import React from 'react';
import { Lightbulb } from 'lucide-react';

interface InsightsCardProps {
  topCategoryName?: string;
  savingPercent?: number;
}

export const InsightsCard: React.FC<InsightsCardProps> = ({
  topCategoryName = 'Food',
  savingPercent = 18,
}) => {
  return (
    <div className="bg-[#F4FBF7] dark:bg-emerald-950/10 border border-[#D1F2DE] dark:border-emerald-900/20 rounded-[20px] p-[20px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[180px] w-full relative overflow-hidden">
      <div>
        {/* Header */}
        <h2 className="text-[13px] font-bold text-[#1e4620] dark:text-emerald-400 flex items-center gap-[6px]">
          <Lightbulb className="w-[15px] h-[15px] text-amber-500 fill-amber-400" />
          Insights
        </h2>
        
        {/* Recommendation text */}
        <p className="text-[12px] font-medium text-[#2d5a30] dark:text-emerald-300 mt-[12px] leading-relaxed max-w-[70%]">
          You spent <span className="font-extrabold text-[#15803d] dark:text-emerald-400">{savingPercent}% less</span> on {topCategoryName} compared to last month.
        </p>
      </div>

      {/* Footer text */}
      <div className="text-[11px] font-extrabold text-[#15803d] dark:text-emerald-400 block pb-[4px]">
        Keep it up!
      </div>

      {/* Decorative Vector Graphic (Right side) */}
      <div className="absolute right-[12px] bottom-[12px] w-[72px] h-[72px] pointer-events-none select-none opacity-90">
        <svg viewBox="0 0 64 64" fill="none" className="w-full h-full">
          {/* Circular bubble container */}
          <circle cx="32" cy="32" r="28" fill="#E8F8F0" opacity="0.8" className="dark:fill-emerald-950/30" />
          
          {/* Little green plant sprout inside bubble */}
          <path d="M32 48 L32 28" stroke="#15803D" strokeWidth="2.5" strokeLinecap="round" />
          {/* Left leaf */}
          <path d="M32 36 C24 36, 24 30, 32 30" fill="#22C55E" opacity="0.9" />
          {/* Right leaf */}
          <path d="M32 28 C40 28, 40 22, 32 22" fill="#10B981" />
          
          {/* Floating gold/green coins representing savings */}
          <circle cx="16" cy="22" r="4" fill="#F59E0B" />
          <circle cx="48" cy="40" r="3" fill="#10B981" />
        </svg>
      </div>
    </div>
  );
};

export default InsightsCard;
