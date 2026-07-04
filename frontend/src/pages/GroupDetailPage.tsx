import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useGroupDashboard } from '../features/group/hooks/useDashboard';
import {
  useArchiveGroup,
  useRestoreGroup,
  useDeleteGroup,
  useLeaveGroup,
  useKickMember,
  useUpdateRole,
  useTransferOwnership,
} from '../features/group/hooks/useGroupMutations';
import { GroupHeader } from '../features/group/components/GroupHeader';
import { MemberList } from '../features/group/components/MemberList';
import { ActivityTimeline } from '../features/group/components/ActivityTimeline';
import { RoomCodeCard } from '../features/group/components/RoomCodeCard';
import { GroupSettingsCard } from '../features/group/components/GroupSettingsCard';
import { GroupDashboardSkeleton } from '../features/group/components/GroupSkeletons';
import { TransferOwnershipDialog } from '../features/group/components/TransferOwnershipDialog';

export const GroupDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthContext();
  const currentUserId = user?.id;

  const [transferTargetId, setTransferTargetId] = React.useState<string | null>(null);

  // Queries
  const { data, isLoading, isError, error } = useGroupDashboard(id || '', !!id);

  // Mutations
  const archiveMutation = useArchiveGroup(id || '');
  const restoreMutation = useRestoreGroup(id || '');
  const deleteMutation = useDeleteGroup();
  const leaveMutation = useLeaveGroup(id || '');
  const kickMutation = useKickMember(id || '');
  const updateRoleMutation = useUpdateRole(id || '');
  const transferOwnershipMutation = useTransferOwnership(id || '');

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6">
        <GroupDashboardSkeleton />
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12 text-center">
        <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 text-red-800 dark:text-red-300 p-4 rounded-xl max-w-md mx-auto">
          <p className="font-semibold mb-2">Failed to load group details</p>
          <p className="text-xs mb-4">{error?.message || 'Access denied or group not found.'}</p>
          <button
            onClick={() => navigate('/dashboard/groups')}
            className="px-4 py-1.5 bg-slate-900 text-white rounded-lg text-sm font-semibold transition-colors"
          >
            Back to Groups
          </button>
        </div>
      </div>
    );
  }

  const { group, members, recentActivities } = data;
  const isArchived = group.settings?.archived;
  const currentUserRole = group.role;

  // Handlers
  const handleKick = (memberId: string) => {
    if (window.confirm('Are you sure you want to remove this member from the group?')) {
      kickMutation.mutate(memberId);
    }
  };

  const handleRoleChange = (memberId: string, role: 'ADMIN' | 'MEMBER') => {
    updateRoleMutation.mutate({ memberId, role });
  };

  const handleTransferClick = (memberId: string) => {
    setTransferTargetId(memberId);
  };

  const handleConfirmTransfer = () => {
    if (transferTargetId) {
      transferOwnershipMutation.mutate(
        { newOwnerId: transferTargetId },
        {
          onSuccess: () => {
            setTransferTargetId(null);
          },
        }
      );
    }
  };

  const handleArchive = () => {
    if (window.confirm('Are you sure you want to archive this group? Future actions will be disabled.')) {
      archiveMutation.mutate();
    }
  };

  const handleRestore = () => {
    restoreMutation.mutate();
  };

  const handleDelete = () => {
    if (
      window.confirm(
        'WARNING: Are you sure you want to delete this group? All shared ledgers and history will be permanently soft deleted.'
      )
    ) {
      deleteMutation.mutate(id || '', {
        onSuccess: () => {
          navigate('/dashboard/groups');
        },
      });
    }
  };

  const handleLeave = () => {
    if (window.confirm('Are you sure you want to leave this group?')) {
      leaveMutation.mutate(undefined, {
        onSuccess: () => {
          navigate('/dashboard/groups');
        },
      });
    }
  };

  const targetMember = members.find((m) => m.userId === transferTargetId);

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 sm:px-6 py-6">
      {/* Header breadcrumb */}
      <GroupHeader group={group} onBackClick={() => navigate('/dashboard/groups')} />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Side: Members list and activity ledger */}
        <div className="lg:col-span-2 space-y-6">
          <MemberList
            members={members}
            currentUserId={currentUserId}
            currentUserRole={currentUserRole}
            isGroupArchived={isArchived}
            onKick={handleKick}
            onRoleChange={handleRoleChange}
            onTransferOwnership={handleTransferClick}
          />

          {/* Timeline Feed Container */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm">
            <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100 mb-4">
              Group Activity Timeline
            </h3>
            <ActivityTimeline activities={recentActivities} />
          </div>
        </div>

        {/* Right Side: Invite Details & Tools */}
        <div className="space-y-6">
          {!isArchived && <RoomCodeCard group={group} />}

          <GroupSettingsCard
            group={group}
            currentUserRole={currentUserRole}
            onArchive={handleArchive}
            onRestore={handleRestore}
            onDelete={handleDelete}
            onLeave={handleLeave}
          />
        </div>
      </div>

      {/* Modal Dialogs */}
      <TransferOwnershipDialog
        isOpen={transferTargetId !== null}
        onClose={() => setTransferTargetId(null)}
        onConfirm={handleConfirmTransfer}
        targetMemberName={targetMember?.userName || ''}
        isLoading={transferOwnershipMutation.isPending}
      />
    </div>
  );
};
export default GroupDetailPage;
