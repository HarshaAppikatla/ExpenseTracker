import React from 'react';

interface StatusBadgeProps {
  text: string;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  text,
  className = '',
}) => {
  const isGreen = ['active', 'verified'].includes(text.toLowerCase());

  const badgeColorClasses = isGreen
    ? 'bg-[#eefcf3] text-[#22c55e] border-[#d1fad7] dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-900/30'
    : 'bg-slate-50 text-slate-500 border-slate-100 dark:bg-slate-800 dark:text-slate-400 dark:border-slate-700';

  return (
    <span className={`inline-flex items-center text-[11px] font-bold px-[10px] py-[3px] rounded-full border tracking-wide uppercase select-none ${badgeColorClasses} ${className}`}>
      <span className={`w-1.5 h-1.5 rounded-full mr-1.5 ${isGreen ? 'bg-[#22c55e] dark:bg-emerald-400' : 'bg-slate-400'}`} />
      {text}
    </span>
  );
};

export default StatusBadge;
