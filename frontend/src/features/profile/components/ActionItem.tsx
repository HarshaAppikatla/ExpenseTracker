import React from 'react';
import { LucideIcon } from 'lucide-react';

interface ActionItemProps {
  title: string;
  description: string;
  icon: LucideIcon;
  iconBgClass: string;
  iconColorClass: string;
  onClick?: () => void;
}

export const ActionItem: React.FC<ActionItemProps> = ({
  title,
  description,
  icon: Icon,
  iconBgClass,
  iconColorClass,
  onClick,
}) => {
  return (
    <button
      onClick={onClick}
      className="h-[96px] w-full text-left bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[18px] p-[20px] shadow-sm hover:shadow-md hover:-translate-y-[2px] transition-all duration-200 flex items-center gap-[16px] group focus:outline-none focus:ring-2 focus:ring-blue-500/20"
    >
      {/* Icon Backdrop */}
      <div className={`w-[44px] h-[44px] rounded-xl flex items-center justify-center shrink-0 ${iconBgClass} ${iconColorClass} group-hover:scale-105 transition-transform duration-200`}>
        <Icon className="w-[20px] h-[20px]" />
      </div>

      {/* Action Metadata */}
      <div className="flex-1 min-w-0">
        <h4 className="text-[13px] font-bold text-slate-900 dark:text-slate-100 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors leading-tight">
          {title}
        </h4>
        <p className="text-[11px] font-medium text-slate-400 dark:text-slate-500 mt-[2px] truncate leading-normal">
          {description}
        </p>
      </div>
    </button>
  );
};

export default ActionItem;
