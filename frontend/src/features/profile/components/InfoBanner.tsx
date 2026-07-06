import React from 'react';
import { ShieldCheck } from 'lucide-react';

interface InfoBannerProps {
  message: string;
}

export const InfoBanner: React.FC<InfoBannerProps> = ({ message }) => {
  return (
    <div className="flex items-start gap-[12px] bg-blue-50/50 dark:bg-slate-900/40 border border-blue-100 dark:border-slate-800 p-[16px] rounded-[16px] text-[12px] text-blue-700 dark:text-blue-400 leading-relaxed mt-[20px] transition-colors duration-200">
      <ShieldCheck className="w-[18px] h-[18px] text-blue-500 shrink-0 mt-[2px]" />
      <span>{message}</span>
    </div>
  );
};

export default InfoBanner;
