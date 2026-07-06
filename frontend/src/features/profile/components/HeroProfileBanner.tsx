import React from 'react';
import { Pencil } from 'lucide-react';
import { ProfileAvatar } from './ProfileAvatar';
import { StatusBadge } from './StatusBadge';

interface HeroProfileBannerProps {
  fullName: string;
  email: string;
  status: string;
  onEditClick?: () => void;
}

export const HeroProfileBanner: React.FC<HeroProfileBannerProps> = ({
  fullName = 'User',
  email = '',
  status = 'ACTIVE',
  onEditClick,
}) => {
  return (
    <div className="h-[280px] w-full bg-gradient-to-r from-[#f8fbff] to-[#eef4ff] dark:from-slate-900/60 dark:to-slate-850/60 border border-[#EAECEF] dark:border-slate-800 rounded-[24px] p-[32px] shadow-sm relative overflow-hidden flex items-center justify-between">
      
      {/* Decorative Wave Overlay SVG Background */}
      <div className="absolute inset-0 pointer-events-none select-none opacity-40 dark:opacity-20 z-0">
        <svg viewBox="0 0 800 280" fill="none" className="w-full h-full object-cover">
          <path
            d="M-50 200 C150 180, 250 240, 450 210 C650 180, 750 250, 850 220 L850 280 L-50 280 Z"
            fill="url(#wave-grad)"
          />
          <defs>
            <linearGradient id="wave-grad" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.08" />
              <stop offset="100%" stopColor="#6366f1" stopOpacity="0.05" />
            </linearGradient>
          </defs>
        </svg>
      </div>

      {/* Left Column: User details (65%) */}
      <div className="flex items-center gap-[24px] w-[65%] z-10">
        <ProfileAvatar fullName={fullName} onEditClick={onEditClick} className="shrink-0" />
        <div className="min-w-0 space-y-[4px]">
          <h1 className="text-[32px] font-extrabold text-slate-900 dark:text-slate-50 tracking-tight leading-none">
            {fullName}
          </h1>
          <p className="text-[16px] font-medium text-slate-600 dark:text-slate-400">
            {email}
          </p>
          <div className="pt-[4px]">
            <StatusBadge text={status} />
          </div>
        </div>
      </div>

      {/* Right Column: Custom SVG Artwork & Edit Profile Button (35%) */}
      <div className="flex flex-col items-end justify-between h-full w-[35%] z-10 relative">
        {/* Floating Edit Button */}
        <button
          onClick={onEditClick}
          className="flex items-center gap-[8px] px-[16px] py-[8px] bg-blue-600 hover:bg-blue-700 dark:bg-blue-600 dark:hover:bg-blue-700 active:scale-95 transition-all text-white font-bold text-[13px] rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 shrink-0 self-end"
        >
          <Pencil className="w-[14px] h-[14px]" />
          <span>Edit Profile</span>
        </button>

        {/* Custom SVG Profile Identity Illustration */}
        <div className="w-[200px] h-[130px] hidden md:block self-end pointer-events-none select-none">
          <svg viewBox="0 0 200 130" fill="none" className="w-full h-full">
            {/* Soft radial highlights */}
            <circle cx="100" cy="65" r="40" fill="#3b82f6" fillOpacity="0.08" filter="blur(10px)" />
            <circle cx="140" cy="50" r="30" fill="#6366f1" fillOpacity="0.08" filter="blur(8px)" />

            {/* Abstract card wave container */}
            <rect x="20" y="20" width="140" height="90" rx="16" fill="url(#card-bg-gradient)" stroke="#FFFFFF" strokeWidth="2.5" className="dark:stroke-slate-800 shadow-lg" />
            
            {/* Mini avatar circle on the card */}
            <circle cx="45" cy="50" r="16" fill="#eef4ff" className="dark:fill-slate-800" />
            <circle cx="45" cy="46" r="6" fill="#60a5fa" />
            <path d="M33 60 C33 54, 57 54, 57 60 Z" fill="#60a5fa" />

            {/* Profile stripes lines representing text details */}
            <rect x="72" y="42" width="60" height="5" rx="2.5" fill="#93c5fd" opacity="0.8" />
            <rect x="72" y="52" width="40" height="5" rx="2.5" fill="#bfdbfe" opacity="0.6" />
            
            {/* Green active status bar badge indicator on card */}
            <rect x="72" y="62" width="25" height="4" rx="2" fill="#34d399" />

            {/* Floating decorative circles & waves */}
            <circle cx="170" cy="35" r="8" fill="#60a5fa" opacity="0.3" />
            <circle cx="180" cy="80" r="12" fill="#818cf8" opacity="0.2" />
            <path d="M10 95 Q25 90, 30 110" stroke="#93c5fd" strokeWidth="2" strokeLinecap="round" opacity="0.4" />
            
            <defs>
              <linearGradient id="card-bg-gradient" x1="20" y1="20" x2="160" y2="110">
                <stop offset="0%" stopColor="#eff6ff" />
                <stop offset="100%" stopColor="#dbeafe" />
              </linearGradient>
            </defs>
          </svg>
        </div>
      </div>
    </div>
  );
};

export default HeroProfileBanner;
