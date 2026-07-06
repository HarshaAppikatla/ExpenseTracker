import React from 'react';
import { LucideIcon } from 'lucide-react';
import { Sparkline } from './Sparkline';

interface MetricCardProps {
  title: string;
  value: string;
  subtext: string | React.ReactNode;
  icon: LucideIcon;
  iconBgClass: string;
  iconColorClass: string;
  sparklineData: number[];
  sparklineColor: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtext,
  icon: Icon,
  iconBgClass,
  iconColorClass,
  sparklineData,
  sparklineColor,
}) => {
  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[160px]">
      <div>
        {/* Top row: Title and Icon */}
        <div className="flex justify-between items-center">
          <span className="text-[14px] font-semibold text-slate-500 dark:text-slate-400">{title}</span>
          <div className={`w-[36px] h-[36px] rounded-full flex items-center justify-center ${iconBgClass} ${iconColorClass} transition-colors duration-200`}>
            <Icon className="w-[18px] h-[18px]" />
          </div>
        </div>
        
        {/* Main metric value */}
        <h3 className="text-[24px] font-extrabold text-slate-900 dark:text-slate-50 tracking-tight mt-[12px]">
          {value}
        </h3>
      </div>

      {/* Bottom row: Subtext and Sparkline */}
      <div className="flex justify-between items-end w-full mt-[8px]">
        <div className="text-[12px] font-medium text-slate-500 dark:text-slate-500 w-[60%] leading-snug">
          {subtext}
        </div>
        <div className="w-[80px] h-[30px] flex items-end">
          <Sparkline data={sparklineData} color={sparklineColor} width={80} height={30} />
        </div>
      </div>
    </div>
  );
};

export default MetricCard;
