import React from 'react';
import { LucideIcon } from 'lucide-react';

interface ProfileStatsCardProps {
  title: string;
  value: string | number;
  subtext: string;
  icon: LucideIcon;
  iconBgClass: string;
  iconColorClass: string;
}

export const ProfileStatsCard: React.FC<ProfileStatsCardProps> = ({
  title,
  value,
  subtext,
  icon: Icon,
  iconBgClass,
  iconColorClass,
}) => {
  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm hover:shadow-md transition-all duration-200 hover:-translate-y-[2px] transform flex flex-col justify-between h-[148px] w-full">
      <div>
        {/* Top row: Title & Icon Backdrop */}
        <div className="flex justify-between items-center">
          <span className="text-[13px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">
            {title}
          </span>
          <div className={`w-[36px] h-[36px] rounded-full flex items-center justify-center ${iconBgClass} ${iconColorClass} transition-colors duration-200`}>
            <Icon className="w-[18px] h-[18px]" />
          </div>
        </div>

        {/* Large Value */}
        <h3 className="text-[24px] font-extrabold text-slate-900 dark:text-slate-50 tracking-tight mt-[12px] truncate">
          {value}
        </h3>
      </div>

      {/* Subtitle */}
      <div className="text-[12px] font-medium text-slate-400 dark:text-slate-500 mt-[4px]">
        {subtext}
      </div>
    </div>
  );
};

export default ProfileStatsCard;
