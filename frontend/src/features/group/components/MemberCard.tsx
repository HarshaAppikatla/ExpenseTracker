import React from 'react';
import { GroupMemberDto, GroupRole } from '../types/group';
import { RoleBadge } from './RoleBadge';
import { UserMinus, Shield, ShieldOff, Crown, Calendar } from 'lucide-react';

interface MemberCardProps {
  member: GroupMemberDto;
  currentUserId?: string;
  currentUserRole: GroupRole;
  isGroupArchived: boolean;
  onKick: (memberId: string) => void;
  onRoleChange: (memberId: string, role: 'ADMIN' | 'MEMBER') => void;
  onTransferOwnership: (memberId: string) => void;
}

export const MemberCard: React.FC<MemberCardProps> = ({
  member,
  currentUserId,
  currentUserRole,
  isGroupArchived,
  onKick,
  onRoleChange,
  onTransferOwnership,
}) => {
  const isSelf = member.userId === currentUserId;

  // Determine actions permissions
  const canKick =
    !isGroupArchived &&
    !isSelf &&
    (currentUserRole === 'OWNER' ||
      (currentUserRole === 'ADMIN' && member.role === 'MEMBER'));

  const canPromote =
    !isGroupArchived &&
    !isSelf &&
    currentUserRole === 'OWNER' &&
    member.role === 'MEMBER';

  const canDemote =
    !isGroupArchived &&
    !isSelf &&
    currentUserRole === 'OWNER' &&
    member.role === 'ADMIN';

  const canTransfer =
    !isGroupArchived &&
    !isSelf &&
    currentUserRole === 'OWNER';

  const formatDate = (dateStr: string) => {
    try {
      return new Date(dateStr).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      });
    } catch (e) {
      return dateStr;
    }
  };

  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 py-4 border-b border-slate-100 dark:border-slate-850 last:border-b-0">
      {/* Member Details */}
      <div className="flex items-center gap-3">
        {/* Avatar placeholder */}
        <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center font-bold text-slate-600 dark:text-slate-300">
          {member.userName.charAt(0).toUpperCase()}
        </div>
        <div className="space-y-1">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-semibold text-slate-800 dark:text-slate-100 text-sm">
              {member.userName} {isSelf && <span className="text-xs text-slate-400 font-normal">(You)</span>}
            </span>
            <RoleBadge role={member.role} />
          </div>
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-400 dark:text-slate-500 font-medium">
            <span>{member.userEmail}</span>
            <span className="flex items-center gap-0.5">
              <Calendar className="w-3.5 h-3.5" />
              Joined {formatDate(member.joinedAt)}
            </span>
          </div>
        </div>
      </div>

      {/* Member Actions */}
      <div className="flex items-center gap-2 sm:self-center">
        {canPromote && (
          <button
            onClick={() => onRoleChange(member.userId, 'ADMIN')}
            className="flex items-center gap-1 px-2.5 py-1.5 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300 rounded-lg text-xs font-semibold transition-colors"
            title="Promote to Admin"
          >
            <Shield className="w-3.5 h-3.5 text-blue-500" />
            Promote
          </button>
        )}

        {canDemote && (
          <button
            onClick={() => onRoleChange(member.userId, 'MEMBER')}
            className="flex items-center gap-1 px-2.5 py-1.5 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300 rounded-lg text-xs font-semibold transition-colors"
            title="Demote to Member"
          >
            <ShieldOff className="w-3.5 h-3.5 text-amber-500" />
            Demote
          </button>
        )}

        {canTransfer && (
          <button
            onClick={() => onTransferOwnership(member.userId)}
            className="flex items-center gap-1 px-2.5 py-1.5 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300 rounded-lg text-xs font-semibold transition-colors"
            title="Transfer ownership"
          >
            <Crown className="w-3.5 h-3.5 text-yellow-500" />
            Make Owner
          </button>
        )}

        {canKick && (
          <button
            onClick={() => onKick(member.userId)}
            className="flex items-center gap-1 px-2.5 py-1.5 border border-red-200 dark:border-red-950 hover:bg-red-50 dark:hover:bg-red-950/20 text-red-600 dark:text-red-400 rounded-lg text-xs font-semibold transition-colors"
            title="Kick member"
          >
            <UserMinus className="w-3.5 h-3.5" />
            Remove
          </button>
        )}
      </div>
    </div>
  );
};
export default MemberCard;
