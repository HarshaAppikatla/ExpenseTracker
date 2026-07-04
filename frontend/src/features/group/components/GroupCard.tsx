import React from 'react';
import { GroupDto } from '../types/group';
import { RoleBadge } from './RoleBadge';
import { Users, CreditCard, ArrowRight, EyeOff } from 'lucide-react';

interface GroupCardProps {
  group: GroupDto;
  onClick: (id: string) => void;
}

export const GroupCard: React.FC<GroupCardProps> = ({ group, onClick }) => {
  const isArchived = group.settings?.archived;

  return (
    <div
      onClick={() => onClick(group.id)}
      className="group relative bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:border-slate-350 dark:hover:border-slate-700 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all duration-300 cursor-pointer flex flex-col justify-between min-h-[190px]"
    >
      {/* Decorative gradient light hover effect */}
      <div className="absolute inset-0 bg-gradient-to-tr from-slate-50 to-transparent dark:from-slate-950/20 dark:to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 rounded-2xl pointer-events-none"></div>

      <div className="relative space-y-3">
        {/* Top header */}
        <div className="flex justify-between items-start gap-4">
          <h3 className="font-bold text-slate-800 dark:text-slate-100 group-hover:text-slate-900 dark:group-hover:text-white text-lg tracking-tight line-clamp-1 transition-colors">
            {group.name}
          </h3>
          <div className="flex items-center gap-1.5 shrink-0">
            {isArchived && (
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-2xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300 border border-amber-200 dark:border-amber-800/40">
                <EyeOff className="w-3 h-3" />
                Archived
              </span>
            )}
            <RoleBadge role={group.role} />
          </div>
        </div>

        {/* Description */}
        <p className="text-slate-500 dark:text-slate-400 text-sm line-clamp-2 leading-relaxed">
          {group.description || 'No description provided.'}
        </p>
      </div>

      {/* Footer metadata */}
      <div className="relative pt-4 border-t border-slate-100 dark:border-slate-850 flex items-center justify-between text-xs text-slate-400 dark:text-slate-500 font-medium">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-1">
            <CreditCard className="w-4 h-4" />
            <span>{group.currency}</span>
          </div>
          <div className="flex items-center gap-1">
            <Users className="w-4 h-4" />
            <span>Code: {group.groupCode}</span>
          </div>
        </div>

        {/* Hover arrow interaction */}
        <span className="flex items-center gap-1 text-slate-500 dark:text-slate-400 font-semibold group-hover:translate-x-1 transition-transform">
          Open
          <ArrowRight className="w-4 h-4" />
        </span>
      </div>
    </div>
  );
};
export default GroupCard;
