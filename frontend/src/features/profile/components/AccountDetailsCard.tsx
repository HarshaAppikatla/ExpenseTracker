import React from 'react';
import { Mail, User, Calendar, ShieldCheck, Activity } from 'lucide-react';
import { StatusBadge } from './StatusBadge';
import { InfoBanner } from './InfoBanner';

interface AccountDetailsCardProps {
  email?: string;
  roles?: string[];
  createdAt?: string;
  loginProvider?: string;
  status?: string;
}

export const AccountDetailsCard: React.FC<AccountDetailsCardProps> = ({
  email = '',
  roles = [],
  createdAt = '',
  loginProvider = 'LOCAL',
  status = 'ACTIVE',
}) => {
  const formattedDate = createdAt
    ? new Date(createdAt).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      })
    : 'July 5, 2026';

  const detailsList = [
    {
      icon: Mail,
      label: 'Email',
      value: email || 'demo@expenseflow.com',
    },
    {
      icon: User,
      label: 'Roles',
      value: roles.map((r) => r.replace('ROLE_', '').toLowerCase()).join(', ') || 'User',
      className: 'capitalize',
    },
    {
      icon: Calendar,
      label: 'Member Since',
      value: formattedDate,
    },
    {
      icon: Activity,
      label: 'Provider',
      value: loginProvider || 'LOCAL',
      className: 'uppercase',
    },
    {
      icon: ShieldCheck,
      label: 'Account Status',
      valueComponent: <StatusBadge text={status || 'ACTIVE'} />,
    },
    {
      icon: ShieldCheck,
      label: 'Email Verified',
      valueComponent: <StatusBadge text="VERIFIED" />,
    },
  ];

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm flex flex-col justify-between min-h-[380px] w-full">
      <div>
        {/* Header */}
        <h2 className="text-[18px] font-bold text-slate-900 dark:text-slate-50 border-b border-slate-100 dark:border-slate-800/80 pb-[16px]">
          Account Details
        </h2>

        {/* Details Grid List */}
        <div className="mt-[16px] divide-y divide-slate-50 dark:divide-slate-850/60">
          {detailsList.map((item, idx) => (
            <div key={idx} className="flex items-center py-[14px] first:pt-0 last:pb-0">
              {/* Label (35%) */}
              <div className="flex items-center gap-[12px] w-[35%] shrink-0 text-slate-500 dark:text-slate-400 font-semibold text-[13px]">
                <item.icon className="w-[16px] h-[16px] text-slate-400 dark:text-slate-500 shrink-0" />
                <span>{item.label}</span>
              </div>
              
              {/* Value (65%) */}
              <div className={`w-[65%] text-slate-900 dark:text-slate-200 font-bold text-[13px] flex items-center ${item.className || ''}`}>
                {item.valueComponent ? item.valueComponent : item.value}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Info Warning Banner */}
      <InfoBanner message="Your account is verified and secured using Spring Security and JWT token rotation. Settings management is disabled for this version." />
    </div>
  );
};

export default AccountDetailsCard;
