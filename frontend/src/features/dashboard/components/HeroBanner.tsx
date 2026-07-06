import React from 'react';
import { ShieldCheck, TrendingUp, Calendar, AlertTriangle } from 'lucide-react';

interface HeroBannerProps {
  fullName: string;
  budgetUtilization: number;
  savingsRate: number;
  activeBillsCount: number;
}

export const HeroBanner: React.FC<HeroBannerProps> = ({
  fullName,
  budgetUtilization,
  savingsRate,
  activeBillsCount,
}) => {
  // Determine budget status display
  const getBudgetStatus = () => {
    if (budgetUtilization >= 100) {
      return { text: 'Exceeded', color: 'text-red-400', icon: AlertTriangle };
    } else if (budgetUtilization >= 80) {
      return { text: 'Warning', color: 'text-amber-400', icon: AlertTriangle };
    } else {
      return { text: 'Healthy', color: 'text-emerald-400', icon: ShieldCheck };
    }
  };

  const budgetStatus = getBudgetStatus();
  const BudgetIcon = budgetStatus.icon;

  return (
    <div className="bg-gradient-to-br from-[#0B1528] via-[#151C3E] to-[#1C1F4D] p-[32px] rounded-[20px] text-white shadow-sm flex flex-col justify-between h-[280px] relative overflow-hidden">
      {/* Background radial highlight */}
      <div className="absolute top-[-50px] right-[-50px] w-[250px] h-[250px] rounded-full bg-blue-600/10 blur-[60px] pointer-events-none" />
      <div className="absolute bottom-[-50px] left-[10%] w-[200px] h-[200px] rounded-full bg-purple-600/10 blur-[50px] pointer-events-none" />

      {/* Main Top Content */}
      <div className="flex justify-between items-start z-10 w-full">
        <div className="max-w-[60%]">
          <h1 className="text-[28px] font-bold tracking-tight text-white leading-tight">
            Good Evening, {fullName} 👋
          </h1>
          <p className="text-[14px] text-slate-400 mt-[8px] font-medium">
            Here's your financial overview for today.
          </p>
        </div>

        {/* Right side Illustration (original vector SVG matching the reference) */}
        <div className="absolute right-[24px] top-[16px] bottom-[16px] w-[240px] hidden md:flex items-center justify-center pointer-events-none select-none">
          <svg viewBox="0 0 240 200" fill="none" className="w-full h-full">
            {/* Background elements */}
            <circle cx="120" cy="100" r="80" fill="url(#hero_bg_grad)" opacity="0.1" />
            <rect x="150" y="40" width="60" height="50" rx="8" fill="#1F2937" opacity="0.4" stroke="#4B5563" strokeWidth="1" />
            <path d="M 160 70 L 180 55 L 200 65" stroke="#10B981" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            <circle cx="200" cy="65" r="3" fill="#10B981" />
            
            {/* Green Potted Plant on Left */}
            <rect x="25" y="115" width="16" height="24" rx="3" fill="#78350F" />
            <path d="M25 115 C20 100, 15 105, 20 95 C25 85, 33 98, 33 115 Z" fill="#10B981" />
            <path d="M33 115 C38 95, 45 100, 42 90 C39 80, 33 95, 33 115 Z" fill="#047857" />
            <path d="M29 115 C24 90, 30 85, 31 80 C32 75, 37 90, 33 115 Z" fill="#059669" />

            {/* Blue Desk */}
            <rect x="20" y="135" width="200" height="8" rx="4" fill="#3B82F6" />
            <rect x="35" y="143" width="8" height="45" fill="#1D4ED8" />
            <rect x="195" y="143" width="8" height="45" fill="#1D4ED8" />
            
            {/* Character sitting at desk */}
            {/* Torso (Purple Hoodie) */}
            <path d="M 105 135 L 145 135 L 140 105 L 110 105 Z" fill="#6366F1" />
            {/* Head */}
            <circle cx="125" cy="85" r="14" fill="#FBCFE8" />
            {/* Hair */}
            <path d="M 111 82 C 111 72, 139 72, 139 82 C 139 74, 125 72, 111 82 Z" fill="#111827" />
            {/* Left Arm & Laptop */}
            <path d="M 110 105 L 98 120 L 115 125" stroke="#6366F1" strokeWidth="8" strokeLinecap="round" strokeLinejoin="round" />
            {/* Laptop */}
            <path d="M 80 135 L 105 135 L 110 120 L 85 120 Z" fill="#4B5563" />
            <path d="M 85 120 L 105 120 L 108 105 L 88 105 Z" fill="#9CA3AF" />
            <rect x="91" y="108" width="14" height="9" fill="#3B82F6" opacity="0.8" />
            {/* Right Arm */}
            <path d="M 140 105 L 152 122 L 138 128" stroke="#6366F1" strokeWidth="8" strokeLinecap="round" strokeLinejoin="round" />

            {/* Coffee mug */}
            <rect x="165" y="123" width="10" height="12" rx="2" fill="#EC4899" />
            <path d="M 175 126 C 178 126, 178 132, 175 132" stroke="#EC4899" strokeWidth="2" />

            {/* Floating details / chart icons */}
            <circle cx="70" cy="55" r="12" fill="#10B981" opacity="0.2" />
            <path d="M 66 55 L 74 55 M 70 51 L 70 59" stroke="#10B981" strokeWidth="2" strokeLinecap="round" />
            
            <circle cx="175" cy="110" r="8" fill="#F59E0B" opacity="0.2" />

            <defs>
              <linearGradient id="hero_bg_grad" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stopColor="#3B82F6" />
                <stop offset="100%" stopColor="#8B5CF6" />
              </linearGradient>
            </defs>
          </svg>
        </div>
      </div>

      {/* Stats row below */}
      <div className="grid grid-cols-3 gap-[24px] pt-[20px] border-t border-slate-800/80 z-10">
        {/* Budget Status */}
        <div className="flex items-center gap-[12px]">
          <div className="w-[36px] h-[36px] rounded-full bg-slate-800/80 flex items-center justify-center text-slate-300">
            <BudgetIcon className={`w-[18px] h-[18px] ${budgetStatus.color}`} />
          </div>
          <div>
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider block">Budget Status</span>
            <span className={`text-[14px] font-bold ${budgetStatus.color}`}>{budgetStatus.text}</span>
          </div>
        </div>

        {/* Savings Rate */}
        <div className="flex items-center gap-[12px]">
          <div className="w-[36px] h-[36px] rounded-full bg-slate-800/80 flex items-center justify-center text-slate-300">
            <TrendingUp className="w-[18px] h-[18px] text-blue-400" />
          </div>
          <div>
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider block">Savings Rate</span>
            <span className="text-[14px] font-bold text-slate-100">{savingsRate.toFixed(0)}% of income</span>
          </div>
        </div>

        {/* Bill Schedules */}
        <div className="flex items-center gap-[12px]">
          <div className="w-[36px] h-[36px] rounded-full bg-slate-800/80 flex items-center justify-center text-slate-300">
            <Calendar className="w-[18px] h-[18px] text-amber-400" />
          </div>
          <div>
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider block">Bill Schedules</span>
            <span className="text-[14px] font-bold text-slate-100">{activeBillsCount} upcoming</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HeroBanner;
