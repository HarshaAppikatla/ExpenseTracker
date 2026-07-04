import React from 'react';
import { GroupRole } from '../types/group';

interface RoleBadgeProps {
  role: GroupRole;
}

export const RoleBadge: React.FC<RoleBadgeProps> = ({ role }) => {
  const styles = {
    OWNER: 'bg-amber-100 text-amber-800 border-amber-200 dark:bg-amber-900/30 dark:text-amber-300 dark:border-amber-800/50',
    ADMIN: 'bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-800/50',
    MEMBER: 'bg-slate-100 text-slate-800 border-slate-200 dark:bg-slate-800/30 dark:text-slate-300 dark:border-slate-700/50',
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${styles[role]}`}>
      {role}
    </span>
  );
};
export default RoleBadge;
