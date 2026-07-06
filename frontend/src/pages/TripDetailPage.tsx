import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useTripDetails } from '../features/trips/hooks/useTripQueries';
import { useGroupDashboard } from '../features/group/hooks/useDashboard';
import {
  useUpdateTrip,
  useUpdateTripStatus,
  useDeleteTrip,
  useAcceptInvite,
  useDeclineInvite,
  useLeaveTrip,
} from '../features/trips/hooks/useTripMutations';
import { TripParticipantList } from '../features/trips/components/TripParticipantList';
import { TripTimeline } from '../features/trips/components/TripTimeline';
import { CreateTripDialog } from '../features/trips/components/CreateTripDialog';
import { ExpenseList } from '../features/expense/components/ExpenseList';
import { CreateTripInput } from '../features/trips/schemas/tripSchemas';
import {
  ArrowLeft,
  Calendar,
  MapPin,
  Clock,
  Edit2,
  Trash2,
  CheckCircle,
  XCircle,
  Play
} from 'lucide-react';

export const TripDetailPage: React.FC = () => {
  const { groupId, tripId } = useParams<{ groupId: string; tripId: string }>();
  const navigate = useNavigate();
  const { user } = useAuthContext();
  const currentUserId = user?.id || '';

  const [isEditOpen, setIsEditOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<'activity' | 'expenses'>('activity');

  // Queries
  const { data: trip, isLoading: isTripLoading, isError: isTripError, error: tripError } = useTripDetails(tripId || '');
  const { data: groupData, isLoading: isGroupLoading } = useGroupDashboard(groupId || '', !!groupId);

  // Mutations
  const updateMutation = useUpdateTrip(tripId || '', groupId || '');
  const statusMutation = useUpdateTripStatus(tripId || '', groupId || '');
  const deleteMutation = useDeleteTrip(groupId || '');
  const acceptMutation = useAcceptInvite(tripId || '', groupId || '');
  const declineMutation = useDeclineInvite(tripId || '', groupId || '');
  const leaveMutation = useLeaveTrip(tripId || '', groupId || '');

  if (isTripLoading || isGroupLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12 animate-pulse space-y-6">
        <div className="h-10 bg-slate-100 dark:bg-slate-800 rounded w-1/4" />
        <div className="h-48 bg-slate-100 dark:bg-slate-800 rounded-2xl" />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 h-96 bg-slate-100 dark:bg-slate-800 rounded-2xl" />
          <div className="h-96 bg-slate-100 dark:bg-slate-800 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (isTripError || !trip || !groupData) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12 text-center">
        <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 text-red-800 dark:text-red-300 p-4 rounded-xl max-w-md mx-auto">
          <p className="font-semibold mb-2">Failed to load trip details</p>
          <p className="text-xs mb-4">{tripError?.message || 'Access denied or trip not found.'}</p>
          <button
            onClick={() => navigate(`/groups/${groupId}`)}
            className="px-4 py-1.5 bg-slate-900 text-white rounded-lg text-sm font-semibold transition-colors"
          >
            Back to Group
          </button>
        </div>
      </div>
    );
  }

  const isOrganizer = trip.organizerId === currentUserId;
  const isCompleted = trip.status === 'COMPLETED';
  const isCancelled = trip.status === 'CANCELLED';
  const isCompletedOrCancelled = isCompleted || isCancelled;

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PLANNING':
        return 'bg-blue-50 text-blue-700 dark:bg-blue-950/30 dark:text-blue-350 border-blue-200 dark:border-blue-900/30';
      case 'ACTIVE':
        return 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/30 dark:text-emerald-350 border-emerald-200 dark:border-emerald-900/30';
      case 'COMPLETED':
        return 'bg-slate-50 text-slate-750 dark:bg-slate-950/30 dark:text-slate-350 border-slate-200 dark:border-slate-800/30';
      case 'CANCELLED':
        return 'bg-red-50 text-red-700 dark:bg-red-950/30 dark:text-red-300 border-red-200 dark:border-red-900/30';
      default:
        return 'bg-slate-50 text-slate-700';
    }
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  // Handlers
  const handleEditSubmit = (input: CreateTripInput) => {
    updateMutation.mutate(
      {
        title: input.title,
        description: input.description,
        destination: {
          city: input.city,
          country: input.country,
          displayName: `${input.city}, ${input.country}`,
        },
        schedule: {
          startDate: input.startDate,
          endDate: input.endDate,
        },
        settings: {
          visibility: input.visibility,
          allowInvites: input.allowInvites,
          archived: trip.settings.archived,
        },
        coverType: input.coverType,
        coverImage: input.coverImage,
      },
      {
        onSuccess: () => {
          setIsEditOpen(false);
        },
      }
    );
  };

  const handleStatusChange = (newStatus: string) => {
    const messages: Record<string, string> = {
      ACTIVE: 'Are you sure you want to START this trip? The schedule dates will become fixed.',
      COMPLETED: 'Are you sure you want to COMPLETE this trip? It will become read-only.',
      CANCELLED: 'Are you sure you want to CANCEL this trip? It will become read-only.',
    };

    if (window.confirm(messages[newStatus] || 'Change trip status?')) {
      statusMutation.mutate(newStatus);
    }
  };

  const handleDelete = () => {
    if (window.confirm('WARNING: Are you sure you want to delete this trip workspace? This action soft-deletes the trip details.')) {
      deleteMutation.mutate(trip.id, {
        onSuccess: () => {
          navigate(`/groups/${groupId}`);
        },
      });
    }
  };

  const handleLeave = () => {
    if (window.confirm('Are you sure you want to leave this trip?')) {
      leaveMutation.mutate();
    }
  };

  const handleAcceptInvite = () => {
    acceptMutation.mutate();
  };

  const handleDeclineInvite = () => {
    if (window.confirm('Are you sure you want to decline this trip invitation?')) {
      declineMutation.mutate();
    }
  };

  const isPreset = trip.coverType === 'PRESET' || trip.coverImage?.startsWith('linear-gradient');

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 sm:px-6 py-6">
      {/* Back button */}
      <button
        onClick={() => navigate(`/groups/${groupId}`)}
        className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-750 dark:text-slate-400 dark:hover:text-slate-200 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Back to {groupData.group.name}
      </button>

      {/* Cover image header banner */}
      <div className="relative rounded-2xl overflow-hidden shadow-sm border border-slate-200 dark:border-slate-800">
        {isPreset && trip.coverImage ? (
          <div style={{ background: trip.coverImage }} className="h-56 w-full" />
        ) : trip.coverImage && !isPreset ? (
          <img
            src={trip.coverImage}
            alt={trip.title}
            className="w-full h-56 object-cover"
          />
        ) : (
          // Beautiful destination-themed gradient fallback
          <div
            className="h-56 w-full"
            style={{
              background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 30%, #0f3460 60%, #533483 100%)',
            }}
          >
            {/* Decorative floating circles */}
            <div className="absolute inset-0 overflow-hidden">
              <div className="absolute -top-12 -right-12 w-56 h-56 rounded-full opacity-20" style={{ background: 'radial-gradient(circle, #e94560, transparent)' }} />
              <div className="absolute -bottom-8 -left-8 w-40 h-40 rounded-full opacity-15" style={{ background: 'radial-gradient(circle, #0f3460, transparent)' }} />
              <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 rounded-full opacity-10" style={{ background: 'radial-gradient(circle, #533483, transparent)' }} />
            </div>
          </div>
        )}
        <div className="absolute inset-0 bg-slate-950/20 dark:bg-black/35" />
        
        <div className="absolute bottom-6 left-6 right-6 text-white flex flex-col md:flex-row md:items-end justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1.5 flex-wrap">
              <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getStatusBadge(trip.status)} bg-white/10 backdrop-blur-md border-white/20`}>
                {trip.status}
              </span>
              <span className="text-xs font-medium text-slate-200 flex items-center gap-1">
                Organizer: <span className="font-semibold text-white">{trip.organizerName}</span>
              </span>
            </div>
            <h2 className="text-2xl font-black">{trip.title}</h2>
          </div>

          {/* Action buttons (Edit/Delete) */}
          {isOrganizer && !isCompletedOrCancelled && (
            <div className="flex gap-2 shrink-0">
              <button
                onClick={() => setIsEditOpen(true)}
                className="flex items-center gap-1 px-3 py-1.5 bg-white/10 hover:bg-white/20 border border-white/10 rounded-lg text-xs font-bold transition-all backdrop-blur-sm"
              >
                <Edit2 className="w-3.5 h-3.5" />
                Edit details
              </button>
              <button
                onClick={handleDelete}
                className="flex items-center gap-1 px-3 py-1.5 bg-red-650 hover:bg-red-550 rounded-lg text-xs font-bold transition-all"
              >
                <Trash2 className="w-3.5 h-3.5" />
                Delete
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: Info & Timeline */}
        <div className="lg:col-span-2 space-y-6">
          
          {/* Details Card */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm space-y-4">
            <h3 className="text-base font-bold text-slate-800 dark:text-slate-100">
              Trip Details
            </h3>

            {trip.description && (
              <p className="text-sm text-slate-600 dark:text-slate-400">
                {trip.description}
              </p>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2 border-t border-slate-100 dark:border-slate-800/60">
              <div className="flex gap-2">
                <MapPin className="w-5 h-5 text-slate-400 dark:text-slate-500 shrink-0" />
                <div>
                  <span className="text-xs font-semibold text-slate-400 dark:text-slate-500 block uppercase tracking-wider">Destination</span>
                  <span className="text-sm text-slate-700 dark:text-slate-200 font-semibold">{trip.destination.displayName}</span>
                </div>
              </div>

              <div className="flex gap-2">
                <Calendar className="w-5 h-5 text-slate-400 dark:text-slate-500 shrink-0" />
                <div>
                  <span className="text-xs font-semibold text-slate-400 dark:text-slate-500 block uppercase tracking-wider">Schedule</span>
                  <div className="text-xs text-slate-700 dark:text-slate-200 font-semibold space-y-0.5">
                    <div>Start: {formatDate(trip.schedule.startDate)}</div>
                    <div>End: {formatDate(trip.schedule.endDate)}</div>
                  </div>
                </div>
              </div>
            </div>

            {/* State Management Panel for Organizer */}
            {isOrganizer && !isCompletedOrCancelled && (
              <div className="bg-slate-50 dark:bg-slate-950/40 border border-slate-100 dark:border-slate-850 p-4 rounded-xl mt-4">
                <span className="text-xs font-bold text-slate-650 dark:text-slate-350 block mb-2 flex items-center gap-1">
                  <Clock className="w-3.5 h-3.5" />
                  Workspace Lifecycle Management
                </span>
                
                <div className="flex gap-2 flex-wrap">
                  {trip.status === 'PLANNING' && (
                    <>
                      <button
                        onClick={() => handleStatusChange('ACTIVE')}
                        className="flex items-center gap-1 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-bold transition-colors"
                      >
                        <Play className="w-3.5 h-3.5" />
                        Start Trip
                      </button>
                      <button
                        onClick={() => handleStatusChange('CANCELLED')}
                        className="flex items-center gap-1 px-3 py-1.5 bg-slate-200 hover:bg-slate-350 dark:bg-slate-800 dark:hover:bg-slate-750 text-slate-700 dark:text-slate-200 rounded-lg text-xs font-semibold transition-colors"
                      >
                        <XCircle className="w-3.5 h-3.5" />
                        Cancel Trip
                      </button>
                    </>
                  )}

                  {trip.status === 'ACTIVE' && (
                    <button
                      onClick={() => handleStatusChange('COMPLETED')}
                      className="flex items-center gap-1 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs font-bold transition-colors"
                    >
                      <CheckCircle className="w-3.5 h-3.5" />
                      Complete Trip
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Timeline & Expenses Tab Feed Container */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm">
            <div className="border-b border-slate-200 dark:border-slate-800/80 flex gap-6 mb-6">
              <button
                onClick={() => setActiveTab('activity')}
                className={`pb-3 text-sm font-bold border-b-2 transition-all ${
                  activeTab === 'activity'
                    ? 'border-slate-900 dark:border-white text-slate-900 dark:text-white'
                    : 'border-transparent text-slate-400 hover:text-slate-650 dark:text-slate-500 dark:hover:text-slate-350'
                }`}
              >
                Workspace Activity Feed
              </button>
              <button
                onClick={() => setActiveTab('expenses')}
                className={`pb-3 text-sm font-bold border-b-2 transition-all ${
                  activeTab === 'expenses'
                    ? 'border-slate-900 dark:border-white text-slate-900 dark:text-white'
                    : 'border-transparent text-slate-400 hover:text-slate-650 dark:text-slate-500 dark:hover:text-slate-350'
                }`}
              >
                Trip Expenses
              </button>
            </div>

            {activeTab === 'activity' && (
              <TripTimeline tripId={trip.id} />
            )}

            {activeTab === 'expenses' && (
              <ExpenseList groupId={groupId || ''} tripId={trip.id} members={groupData.members} />
            )}
          </div>

        </div>

        {/* Right Column: Participants list */}
        <div className="space-y-6">
          <TripParticipantList
            trip={trip}
            groupMembers={groupData.members}
            currentUserId={currentUserId}
            isCompletedOrCancelled={isCompletedOrCancelled}
            onLeaveTrip={handleLeave}
            onAcceptInvite={handleAcceptInvite}
            onDeclineInvite={handleDeclineInvite}
          />
        </div>
      </div>

      {/* Edit details dialog */}
      <CreateTripDialog
        isOpen={isEditOpen}
        onClose={() => setIsEditOpen(false)}
        onSubmit={handleEditSubmit}
        isLoading={updateMutation.isPending}
      />
    </div>
  );
};
export default TripDetailPage;
