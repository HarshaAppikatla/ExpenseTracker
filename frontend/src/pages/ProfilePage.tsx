import React from 'react';
import { Card } from '@/components/ui/Card';
import { User, Mail, ShieldCheck, Calendar, Activity } from 'lucide-react';
import { Avatar } from '@/components/ui/Avatar';
import { useAuthContext } from '@/hooks/useAuthContext';

export const ProfilePage: React.FC = () => {
  const { user } = useAuthContext();

  const formattedDate = user?.createdAt 
    ? new Date(user.createdAt).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    : 'N/A';

  return (
    <div className="max-w-2xl mx-auto space-y-24">
      <Card className="flex flex-col md:flex-row items-center gap-24 p-32">
        <Avatar name={user?.fullName || 'User'} size="lg" className="border-4 border-primary/20" />
        <div className="space-y-4 text-center md:text-left">
          <h2 className="text-xl font-bold text-light-text dark:text-dark-text">
            {user?.fullName}
          </h2>
          <p className="text-sm text-light-textSecondary dark:text-slate-400">
            {user?.email}
          </p>
          <div className="flex flex-wrap gap-8 justify-center md:justify-start mt-2">
            <span className="px-10 py-2 bg-slate-100 dark:bg-slate-800 text-light-textSecondary dark:text-slate-400 text-xs rounded-full border border-light-border dark:border-dark-border font-semibold">
              Status: {user?.status}
            </span>
          </div>
        </div>
      </Card>

      <Card className="p-24 space-y-16">
        <h3 className="font-bold text-sm text-light-text dark:text-dark-text border-b border-light-border dark:border-dark-border pb-12">
          Account Details
        </h3>
        
        <div className="space-y-12">
          <div className="flex items-center gap-12 text-sm">
            <Mail className="w-16 h-16 text-light-textSecondary dark:text-slate-400" />
            <span className="text-light-textSecondary dark:text-slate-400 w-[90px]">Email:</span>
            <span className="font-semibold text-light-text dark:text-dark-text">
              {user?.email}
            </span>
          </div>
 
          <div className="flex items-center gap-12 text-sm">
            <User className="w-16 h-16 text-light-textSecondary dark:text-slate-400" />
            <span className="text-light-textSecondary dark:text-slate-400 w-[90px]">Roles:</span>
            <span className="font-semibold text-light-text dark:text-dark-text capitalize">
              {user?.roles?.map(r => r.replace('ROLE_', '').toLowerCase()).join(', ')}
            </span>
          </div>
 
          <div className="flex items-center gap-12 text-sm">
            <Calendar className="w-16 h-16 text-light-textSecondary dark:text-slate-400" />
            <span className="text-light-textSecondary dark:text-slate-400 w-[90px]">Created:</span>
            <span className="font-semibold text-light-text dark:text-dark-text">
              {formattedDate}
            </span>
          </div>
 
          <div className="flex items-center gap-12 text-sm">
            <Activity className="w-16 h-16 text-light-textSecondary dark:text-slate-400" />
            <span className="text-light-textSecondary dark:text-slate-400 w-[90px]">Provider:</span>
            <span className="font-semibold text-light-text dark:text-dark-text uppercase">
              {user?.loginProvider}
            </span>
          </div>
        </div>
 
        <div className="flex items-start gap-8 bg-blue-50 dark:bg-slate-900 border border-blue-100 dark:border-slate-800 p-12 rounded-btn text-xs text-primary mt-16 leading-relaxed">
          <ShieldCheck className="w-16 h-16 mt-2 flex-shrink-0" />
          <span>
            Your account is verified and secured using Spring Security and JWT token rotation. Settings management is disabled for this version.
          </span>
        </div>
      </Card>
    </div>
  );
};
