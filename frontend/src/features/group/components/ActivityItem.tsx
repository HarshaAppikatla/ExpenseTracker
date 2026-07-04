import React from 'react';
import { GroupActivityDto, ActivityType } from '../types/group';
import {
  PlusCircle,
  Settings,
  Archive,
  RefreshCw,
  Trash2,
  UserPlus,
  UserMinus,
  Shield,
  Crown,
  Info,
} from 'lucide-react';

interface ActivityItemProps {
  activity: GroupActivityDto;
}

export const ActivityItem: React.FC<ActivityItemProps> = ({ activity }) => {
  const getIcon = (type: ActivityType) => {
    const iconSizeClass = "w-4 h-4";
    switch (type) {
      case 'GROUP_CREATED':
        return <PlusCircle className={`${iconSizeClass} text-green-500`} />;
      case 'GROUP_UPDATED':
        return <Settings className={`${iconSizeClass} text-blue-500`} />;
      case 'GROUP_ARCHIVED':
        return <Archive className={`${iconSizeClass} text-amber-500`} />;
      case 'GROUP_RESTORED':
        return <RefreshCw className={`${iconSizeClass} text-indigo-500`} />;
      case 'GROUP_DELETED':
        return <Trash2 className={`${iconSizeClass} text-red-500`} />;
      case 'MEMBER_JOINED':
        return <UserPlus className={`${iconSizeClass} text-emerald-500`} />;
      case 'MEMBER_LEFT':
      case 'MEMBER_REMOVED':
        return <UserMinus className={`${iconSizeClass} text-slate-500`} />;
      case 'ROLE_CHANGED':
        return <Shield className={`${iconSizeClass} text-indigo-500`} />;
      case 'OWNER_TRANSFERRED':
        return <Crown className={`${iconSizeClass} text-yellow-500`} />;
      default:
        return <Info className={`${iconSizeClass} text-slate-400`} />;
    }
  };

  const renderContent = (type: ActivityType, metadata: Record<string, any>, createdBy: string) => {
    const actor = metadata.actorName || createdBy || 'Someone';
    const target = metadata.targetUserName || 'someone';

    switch (type) {
      case 'GROUP_CREATED':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> created the group.
          </span>
        );
      case 'GROUP_UPDATED':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> updated the group settings.
          </span>
        );
      case 'GROUP_ARCHIVED':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> archived this group.
          </span>
        );
      case 'GROUP_RESTORED':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> restored this group from archive.
          </span>
        );
      case 'GROUP_DELETED':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> deleted this group.
          </span>
        );
      case 'MEMBER_JOINED':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> joined the group.
          </span>
        );
      case 'MEMBER_LEFT':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> left the group.
          </span>
        );
      case 'MEMBER_REMOVED':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> removed{' '}
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{target}</strong> from the group.
          </span>
        );
      case 'ROLE_CHANGED': {
        const newRole = metadata.newRole || 'MEMBER';
        const isPromotion = newRole === 'ADMIN';
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> {isPromotion ? 'promoted' : 'demoted'}{' '}
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{target}</strong> to{' '}
            <span className="text-xs px-1.5 py-0.5 rounded font-mono bg-slate-100 dark:bg-slate-850 text-slate-700 dark:text-slate-350 border border-slate-200 dark:border-slate-800">
              {newRole}
            </span>
          </span>
        );
      }
      case 'OWNER_TRANSFERRED':
        return (
          <span>
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{actor}</strong> transferred group ownership to{' '}
            <strong className="font-semibold text-slate-800 dark:text-slate-200">{target}</strong>.
          </span>
        );
      default:
        return <span>Activity log event recorded: {type}</span>;
    }
  };

  const formatDate = (dateStr: string) => {
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch (e) {
      return dateStr;
    }
  };

  return (
    <div className="flex gap-4">
      {/* Icon node */}
      <div className="w-8 h-8 rounded-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-850 flex items-center justify-center shrink-0 shadow-sm">
        {getIcon(activity.actionType)}
      </div>

      {/* Content block */}
      <div className="flex-1 pt-1">
        <p className="text-sm text-slate-600 dark:text-slate-300">
          {renderContent(activity.actionType, activity.metadata || {}, activity.createdBy)}
        </p>
        <span className="text-xs text-slate-400 dark:text-slate-500 font-medium block mt-0.5">
          {formatDate(activity.createdAt)}
        </span>
      </div>
    </div>
  );
};
export default ActivityItem;
