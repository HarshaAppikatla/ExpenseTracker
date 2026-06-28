import React from 'react';
import { Card } from '@/components/ui/Card';
import { User, Mail, ShieldAlert } from 'lucide-react';
import { Avatar } from '@/components/ui/Avatar';

export const ProfilePage: React.FC = () => {
  return (
    <div className="max-w-2xl mx-auto space-y-24">
      <Card className="flex flex-col md:flex-row items-center gap-24 p-32">
        <Avatar name="Jane Doe" size="lg" className="border-4 border-primary/20" />
        <div className="space-y-4 text-center md:text-left">
          <h2 className="text-xl font-bold text-light-text dark:text-dark-text">Jane Doe</h2>
          <p className="text-sm text-light-textSecondary dark:text-slate-400">Collaborator & Planner</p>
          <div className="flex flex-wrap gap-8 justify-center md:justify-start">
            <span className="px-10 py-2 bg-slate-100 dark:bg-slate-800 text-light-textSecondary dark:text-slate-400 text-xs rounded-full border border-light-border dark:border-dark-border">
              Developer Profile
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
            <Mail className="w-4 h-4 text-light-textSecondary dark:text-slate-400" />
            <span className="text-light-textSecondary dark:text-slate-400 w-24">Email:</span>
            <span className="font-semibold text-light-text dark:text-dark-text">jane.doe@expenseflow.com</span>
          </div>

          <div className="flex items-center gap-12 text-sm">
            <User className="w-4 h-4 text-light-textSecondary dark:text-slate-400" />
            <span className="text-light-textSecondary dark:text-slate-400 w-24">Role:</span>
            <span className="font-semibold text-light-text dark:text-dark-text">System Architect</span>
          </div>
        </div>

        <div className="flex items-start gap-8 bg-blue-50 dark:bg-slate-905 border border-blue-100 dark:border-slate-800 p-12 rounded-btn text-xs text-primary mt-16 leading-relaxed">
          <ShieldAlert className="w-4 h-4 mt-2 flex-shrink-0" />
          <span>
            This profile is a local foundation template. Authentication structures and editable details will be implemented in Sprint 01.
          </span>
        </div>
      </Card>
    </div>
  );
};
