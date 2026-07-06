import React from 'react';
import { LucideIcon } from 'lucide-react';

interface TimelineItemProps {
  title: string;
  timestamp: string;
  icon: LucideIcon;
  iconBgClass: string;
  iconColorClass: string;
  badgeText?: string;
  isLast?: boolean;
}

export const TimelineItem: React.FC<TimelineItemProps> = ({
  title,
  timestamp,
  icon: Icon,
  iconBgClass,
  iconColorClass,
  badgeText,
  isLast = false,
}) => {
  return (
    <div className="flex gap-[16px] relative select-none">
      {/* Connecting Vertical Line */}
      {!isLast && (
        <div className="absolute left-[20px] top-[40px] bottom-[-24px] w-[2px] bg-slate-100 dark:bg-slate-800" />
      )}
      
      {/* Circle Icon */}
      <div className={`w-[40px] h-[40px] rounded-full flex items-center justify-center shrink-0 z-10 ${iconBgClass} ${iconColorClass}`}>
        <Icon className="w-[18px] h-[18px]" />
      </div>

      {/* Content details */}
      <div className="flex-1 min-w-0 pt-[2px]">
        <div className="flex items-center justify-between gap-[8px]">
          <h4 className="text-[13px] font-bold text-slate-850 dark:text-slate-200 leading-snug">
            {title}
          </h4>
          {badgeText && (
            <span className="text-[9px] font-extrabold px-[6px] py-[1.5px] rounded bg-emerald-50 text-emerald-600 border border-emerald-100 dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-950/30">
              {badgeText}
            </span>
          )}
        </div>
        <p className="text-[11px] text-slate-400 dark:text-slate-500 mt-[3px]">
          {timestamp}
        </p>
      </div>
    </div>
  );
};

export default TimelineItem;
