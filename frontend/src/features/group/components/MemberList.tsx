import React from 'react';
import { GroupMemberDto, GroupRole } from '../types/group';
import { MemberCard } from './MemberCard';

interface MemberListProps {
  members: GroupMemberDto[];
  currentUserId?: string;
  currentUserRole: GroupRole;
  isGroupArchived: boolean;
  onKick: (memberId: string) => void;
  onRoleChange: (memberId: string, role: 'ADMIN' | 'MEMBER') => void;
  onTransferOwnership: (memberId: string) => void;
}

export const MemberList: React.FC<MemberListProps> = ({
  members,
  currentUserId,
  currentUserRole,
  isGroupArchived,
  onKick,
  onRoleChange,
  onTransferOwnership,
}) => {
  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm">
      <h2 className="text-lg font-bold text-slate-800 dark:text-slate-100 mb-4 flex items-center justify-between">
        <span>Group Members</span>
        <span className="text-xs px-2 py-0.5 rounded-full font-semibold bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400">
          {members.length} Active
        </span>
      </h2>
      <div className="divide-y divide-slate-100 dark:divide-slate-850">
        {members.map((member) => (
          <MemberCard
            key={member.id}
            member={member}
            currentUserId={currentUserId}
            currentUserRole={currentUserRole}
            isGroupArchived={isGroupArchived}
            onKick={onKick}
            onRoleChange={onRoleChange}
            onTransferOwnership={onTransferOwnership}
          />
        ))}
      </div>
    </div>
  );
};
export default MemberList;
