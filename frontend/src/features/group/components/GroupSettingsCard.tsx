import React from 'react';
import { GroupDto, GroupRole } from '../types/group';
import { RefreshCw, Trash2, LogOut, EyeOff } from 'lucide-react';

interface GroupSettingsCardProps {
  group: GroupDto;
  currentUserRole: GroupRole;
  onArchive: () => void;
  onRestore: () => void;
  onDelete: () => void;
  onLeave: () => void;
}

export const GroupSettingsCard: React.FC<GroupSettingsCardProps> = ({
  group,
  currentUserRole,
  onArchive,
  onRestore,
  onDelete,
  onLeave,
}) => {
  const isArchived = group.settings?.archived;
  const isOwner = currentUserRole === 'OWNER';
  const isAdminOrOwner = currentUserRole === 'OWNER' || currentUserRole === 'ADMIN';

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm">
      <h3 className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-4">
        Group Administration
      </h3>
      <div className="space-y-3">
        {/* Archive/Restore Actions */}
        {isAdminOrOwner && (
          <>
            {isArchived ? (
              <button
                onClick={onRestore}
                className="w-full flex items-center justify-center gap-2 px-4 py-2 border border-indigo-200 dark:border-indigo-950 hover:bg-indigo-50 dark:hover:bg-indigo-950/20 text-indigo-600 dark:text-indigo-400 rounded-lg text-sm font-semibold transition-colors"
              >
                <RefreshCw className="w-4 h-4" />
                Restore Group
              </button>
            ) : (
              <button
                onClick={onArchive}
                className="w-full flex items-center justify-center gap-2 px-4 py-2 border border-amber-250 dark:border-amber-950 hover:bg-amber-50 dark:hover:bg-amber-950/20 text-amber-600 dark:text-amber-400 rounded-lg text-sm font-semibold transition-colors"
              >
                <EyeOff className="w-4 h-4" />
                Archive Group
              </button>
            )}
          </>
        )}

        {/* Leave Action */}
        {!isOwner && (
          <button
            onClick={onLeave}
            className="w-full flex items-center justify-center gap-2 px-4 py-2 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 rounded-lg text-sm font-semibold transition-colors"
          >
            <LogOut className="w-4 h-4" />
            Leave Group
          </button>
        )}

        {/* Delete Action */}
        {isOwner && (
          <button
            onClick={onDelete}
            className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-red-650 hover:bg-red-700 text-white rounded-lg text-sm font-semibold transition-colors"
          >
            <Trash2 className="w-4 h-4" />
            Delete Group
          </button>
        )}
      </div>
    </div>
  );
};
export default GroupSettingsCard;
