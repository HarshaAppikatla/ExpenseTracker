import React from 'react';
import { GroupDto } from '../types/group';
import { RoleBadge } from './RoleBadge';
import { ChevronLeft, Calendar, User, BadgeAlert } from 'lucide-react';

interface GroupHeaderProps {
  group: GroupDto;
  onBackClick: () => void;
}

export const GroupHeader: React.FC<GroupHeaderProps> = ({ group, onBackClick }) => {
  const isArchived = group.settings?.archived;

  return (
    <div className="space-y-4">
      {/* Back button */}
      <button
        onClick={onBackClick}
        className="flex items-center gap-1 text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 text-sm font-medium transition-colors"
      >
        <ChevronLeft className="w-4 h-4" />
        Back to Groups
      </button>

      {/* Main header block */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
        <div className="space-y-2.5">
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl font-bold text-slate-800 dark:text-slate-100 tracking-tight">
              {group.name}
            </h1>
            <div className="flex items-center gap-1.5">
              {isArchived && (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300 border border-amber-200 dark:border-amber-800/40">
                  <BadgeAlert className="w-3.5 h-3.5" />
                  Archived
                </span>
              )}
              <RoleBadge role={group.role} />
            </div>
          </div>

          <p className="text-slate-500 dark:text-slate-400 text-sm max-w-2xl leading-relaxed">
            {group.description || 'No description provided.'}
          </p>

          <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400 dark:text-slate-500 font-semibold pt-1">
            <span className="flex items-center gap-1">
              <User className="w-3.5 h-3.5" />
              Owner: {group.ownerName}
            </span>
            <span className="flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5" />
              Currency: {group.currency}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
export default GroupHeader;
