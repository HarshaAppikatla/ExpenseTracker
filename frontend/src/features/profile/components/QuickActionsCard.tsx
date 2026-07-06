import React from 'react';
import { Lock, Mail, Bell, CloudDownload } from 'lucide-react';
import { ActionItem } from './ActionItem';

export const QuickActionsCard: React.FC = () => {
  const actions = [
    {
      title: 'Change Password',
      description: 'Update your account password',
      icon: Lock,
      iconBgClass: 'bg-blue-50/80 dark:bg-blue-950/20',
      iconColorClass: 'text-blue-600 dark:text-blue-400',
      onClick: () => console.log('Change Password Clicked'),
    },
    {
      title: 'Update Email',
      description: 'Change your email address',
      icon: Mail,
      iconBgClass: 'bg-blue-50/80 dark:bg-blue-950/20',
      iconColorClass: 'text-blue-600 dark:text-blue-400',
      onClick: () => console.log('Update Email Clicked'),
    },
    {
      title: 'Notification Settings',
      description: 'Manage your preferences',
      icon: Bell,
      iconBgClass: 'bg-blue-50/80 dark:bg-blue-950/20',
      iconColorClass: 'text-blue-600 dark:text-blue-400',
      onClick: () => console.log('Notifications Clicked'),
    },
    {
      title: 'Download My Data',
      description: 'Export your account data',
      icon: CloudDownload,
      iconBgClass: 'bg-blue-50/80 dark:bg-blue-950/20',
      iconColorClass: 'text-blue-600 dark:text-blue-400',
      onClick: () => console.log('Download Data Clicked'),
    },
  ];

  return (
    <div className="w-full">
      {/* Title */}
      <h3 className="text-[14px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wide mb-[12px] select-none">
        Quick Actions
      </h3>

      {/* Grid wrapper */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[24px]">
        {actions.map((act, idx) => (
          <ActionItem
            key={idx}
            title={act.title}
            description={act.description}
            icon={act.icon}
            iconBgClass={act.iconBgClass}
            iconColorClass={act.iconColorClass}
            onClick={act.onClick}
          />
        ))}
      </div>
    </div>
  );
};

export default QuickActionsCard;
