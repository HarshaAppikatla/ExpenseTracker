import React, { useState } from 'react';
import { TripDto } from '../types/trip';
import { GroupMemberDto } from '../../group/types/group';
import {
  useInviteParticipant,
  useRemoveParticipant
} from '../hooks/useTripMutations';
import { UserPlus, UserMinus, Shield, Check, X, LogOut } from 'lucide-react';

interface TripParticipantListProps {
  trip: TripDto;
  groupMembers: GroupMemberDto[];
  currentUserId: string;
  isCompletedOrCancelled: boolean;
  onLeaveTrip: () => void;
  onAcceptInvite: () => void;
  onDeclineInvite: () => void;
}

export const TripParticipantList: React.FC<TripParticipantListProps> = ({
  trip,
  groupMembers,
  currentUserId,
  isCompletedOrCancelled,
  onLeaveTrip,
  onAcceptInvite,
  onDeclineInvite,
}) => {
  const [selectedUserId, setSelectedUserId] = useState('');
  const [isInviteOpen, setIsInviteOpen] = useState(false);

  // Mutations
  const inviteMutation = useInviteParticipant(trip.id, trip.groupId);
  const removeMutation = useRemoveParticipant(trip.id, trip.groupId);

  const isOrganizer = trip.organizerId === currentUserId;

  // Filter group members who are not already in the trip
  const currentParticipantIds = trip.participants.map((p) => p.userId);
  const inviteableMembers = groupMembers.filter(
    (m) => !currentParticipantIds.includes(m.userId)
  );

  const handleInvite = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUserId) return;
    
    inviteMutation.mutate(selectedUserId, {
      onSuccess: () => {
        setSelectedUserId('');
        setIsInviteOpen(false);
      },
    });
  };

  const handleRemove = (userId: string) => {
    if (window.confirm('Are you sure you want to remove this participant from the trip?')) {
      removeMutation.mutate(userId);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
      case 'JOINED':
        return 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/20 dark:text-emerald-350 border-emerald-100 dark:border-emerald-900/20';
      case 'INVITED':
        return 'bg-amber-50 text-amber-700 dark:bg-amber-950/20 dark:text-amber-355 border-amber-100 dark:border-amber-900/20';
      case 'DECLINED':
        return 'bg-rose-50 text-rose-700 dark:bg-rose-950/20 dark:text-rose-355 border-rose-100 dark:border-rose-900/20';
      case 'LEFT':
        return 'bg-slate-50 text-slate-500 dark:bg-slate-900 dark:text-slate-400 border-slate-100 dark:border-slate-800';
      case 'REMOVED':
        return 'bg-slate-100 text-slate-500 dark:bg-slate-850 dark:text-slate-400 border-slate-200 dark:border-slate-800';
      default:
        return 'bg-slate-50 text-slate-700';
    }
  };

  // Determine current user's participant record
  const myParticipantRecord = trip.participants.find((p) => p.userId === currentUserId);
  const isInvited = myParticipantRecord?.status === 'INVITED';
  const isJoined = myParticipantRecord?.status === 'ACCEPTED';

  // Can invite: organizer, or joined member with allowInvites setting enabled — only if there are members left to invite
  const canInvite = !isCompletedOrCancelled &&
    inviteableMembers.length > 0 &&
    (isOrganizer || (isJoined && trip.settings.allowInvites));

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm space-y-6">
      <div className="flex justify-between items-center">
        <h3 className="text-base font-bold text-slate-800 dark:text-slate-100">
          Trip Participants ({trip.participants.filter(p => p.status === 'ACCEPTED').length})
        </h3>

        {canInvite && (
          <button
            onClick={() => setIsInviteOpen(!isInviteOpen)}
            className="flex items-center gap-1 px-3 py-1 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition-colors"
          >
            <UserPlus className="w-3.5 h-3.5" />
            Invite
          </button>
        )}
      </div>

      {/* Invite Form */}
      {isInviteOpen && (
        <form onSubmit={handleInvite} className="flex gap-2 p-3 bg-slate-50 dark:bg-slate-950/40 rounded-lg border border-slate-100 dark:border-slate-855/80">
          <select
            value={selectedUserId}
            onChange={(e) => setSelectedUserId(e.target.value)}
            className="flex-1 px-3 py-1.5 border border-slate-200 dark:border-slate-800 rounded-lg bg-white dark:bg-slate-950 text-slate-850 dark:text-slate-100 text-xs focus:outline-none focus:ring-2 focus:ring-slate-100"
            disabled={inviteMutation.isPending}
          >
            <option value="">Select group member...</option>
            {inviteableMembers.map((m) => (
              <option key={m.userId} value={m.userId}>
                {m.userName} ({m.userEmail})
              </option>
            ))}
          </select>

          <button
            type="submit"
            className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition-colors shrink-0 disabled:opacity-50"
            disabled={!selectedUserId || inviteMutation.isPending}
          >
            Send Invite
          </button>
        </form>
      )}

      {/* Invitation actions for current user */}
      {isInvited && !isCompletedOrCancelled && (
        <div className="bg-blue-50/50 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/30 p-4 rounded-xl space-y-3">
          <p className="text-xs text-blue-800 dark:text-blue-300 font-medium">
            You have been invited to join this trip workspace.
          </p>
          <div className="flex gap-2">
            <button
              onClick={onAcceptInvite}
              className="flex items-center gap-1 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-bold transition-colors"
            >
              <Check className="w-3.5 h-3.5" />
              Join Trip
            </button>
            <button
              onClick={onDeclineInvite}
              className="flex items-center gap-1 px-3 py-1.5 bg-slate-200 hover:bg-slate-350 dark:bg-slate-800 dark:hover:bg-slate-750 text-slate-700 dark:text-slate-200 rounded-lg text-xs font-semibold transition-colors"
            >
              <X className="w-3.5 h-3.5" />
              Decline
            </button>
          </div>
        </div>
      )}

      {/* Participants List */}
      <div className="divide-y divide-slate-100 dark:divide-slate-800/60">
        {trip.participants.map((participant) => {
          const isUserOrg = participant.userId === trip.organizerId;
          const isMe = participant.userId === currentUserId;

          return (
            <div key={participant.id} className="py-3 flex items-center justify-between first:pt-0 last:pb-0 group">
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                    {participant.userName}
                    {isMe && ' (You)'}
                  </span>
                  {isUserOrg && (
                    <span className="inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded text-[9px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
                      <Shield className="w-2.5 h-2.5" />
                      Organizer
                    </span>
                  )}
                </div>
                <span className="text-xs text-slate-450 dark:text-slate-500 font-medium block">
                  {participant.userEmail}
                </span>
              </div>

              <div className="flex items-center gap-3">
                <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getStatusBadge(participant.status)}`}>
                  {participant.status}
                </span>

                {/* Management controls */}
                {!isCompletedOrCancelled && (
                  <>
                    {/* Kick participant */}
                    {isOrganizer && !isUserOrg && participant.status !== 'LEFT' && participant.status !== 'REMOVED' && (
                      <button
                        onClick={() => handleRemove(participant.userId)}
                        className="p-1 hover:bg-red-50 dark:hover:bg-red-950/20 text-slate-400 hover:text-red-500 rounded transition-colors"
                        title="Remove participant"
                      >
                        <UserMinus className="w-3.5 h-3.5" />
                      </button>
                    )}

                    {/* Leave trip */}
                    {isMe && !isUserOrg && participant.status === 'ACCEPTED' && (
                      <button
                        onClick={onLeaveTrip}
                        className="flex items-center gap-1 px-2.5 py-1 border border-slate-200 dark:border-slate-800 hover:border-red-200 hover:bg-red-50/20 dark:hover:border-red-950/20 hover:text-red-650 rounded-lg text-xs font-semibold text-slate-650 dark:text-slate-350 transition-all"
                      >
                        <LogOut className="w-3 h-3" />
                        Leave
                      </button>
                    )}
                  </>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
export default TripParticipantList;
