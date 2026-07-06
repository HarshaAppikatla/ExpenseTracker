import React from 'react';
import { useTripTimeline } from '../hooks/useTripQueries';
import {
  PlusCircle,
  Settings,
  Play,
  CheckCircle,
  XCircle,
  Trash2,
  UserPlus,
  UserCheck,
  UserMinus,
  Info
} from 'lucide-react';
import { TripActivityDto } from '../types/trip';

interface TripTimelineProps {
  tripId: string;
}

export const TripTimeline: React.FC<TripTimelineProps> = ({ tripId }) => {
  const { data, isLoading } = useTripTimeline(tripId, 0, 50);

  if (isLoading) {
    return (
      <div className="space-y-4 animate-pulse">
        {[...Array(3)].map((_, i) => (
          <div key={i} className="flex gap-4">
            <div className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-800 shrink-0" />
            <div className="flex-1 space-y-2 py-1">
              <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-3/4" />
              <div className="h-3 bg-slate-100 dark:bg-slate-800 rounded w-1/4" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  const activities = data?.content || [];

  if (activities.length === 0) {
    return (
      <div className="text-center py-8 text-slate-450 text-xs font-semibold">
        No activity logged yet.
      </div>
    );
  }

  const getIcon = (type: string) => {
    const iconClass = "w-4 h-4";
    switch (type) {
      case 'TRIP_CREATED':
        return <PlusCircle className={`${iconClass} text-green-500`} />;
      case 'TRIP_UPDATED':
        return <Settings className={`${iconClass} text-blue-500`} />;
      case 'TRIP_STARTED':
        return <Play className={`${iconClass} text-emerald-500`} />;
      case 'TRIP_COMPLETED':
        return <CheckCircle className={`${iconClass} text-indigo-500`} />;
      case 'TRIP_CANCELLED':
        return <XCircle className={`${iconClass} text-red-500`} />;
      case 'TRIP_DELETED':
        return <Trash2 className={`${iconClass} text-red-650`} />;
      case 'PARTICIPANT_INVITED':
        return <UserPlus className={`${iconClass} text-blue-400`} />;
      case 'PARTICIPANT_JOINED':
        return <UserCheck className={`${iconClass} text-emerald-500`} />;
      case 'PARTICIPANT_DECLINED':
        return <UserMinus className={`${iconClass} text-slate-400`} />;
      case 'PARTICIPANT_LEFT':
      case 'PARTICIPANT_REMOVED':
        return <UserMinus className={`${iconClass} text-slate-500`} />;
      default:
        return <Info className={`${iconClass} text-slate-450`} />;
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
    <div className="relative">
      {/* Timeline connector line */}
      <div className="absolute left-4 top-4 bottom-4 w-0.5 bg-slate-100 dark:bg-slate-800 pointer-events-none" />

      <div className="space-y-6">
        {activities.map((activity: TripActivityDto) => (
          <div key={activity.id} className="flex gap-4">
            <div className="w-8 h-8 rounded-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-850 flex items-center justify-center shrink-0 shadow-sm z-10">
              {getIcon(activity.activityType)}
            </div>

            <div className="flex-1 pt-1">
              <p className="text-sm text-slate-650 dark:text-slate-350 leading-relaxed">
                <span className="font-semibold text-slate-800 dark:text-slate-100">
                  {activity.actorName}
                </span>{' '}
                {activity.activityType === 'TRIP_CREATED' && 'created the trip'}
                {activity.activityType === 'TRIP_UPDATED' && 'updated the trip details'}
                {activity.activityType === 'TRIP_STARTED' && 'started the trip'}
                {activity.activityType === 'TRIP_COMPLETED' && 'completed the trip'}
                {activity.activityType === 'TRIP_CANCELLED' && 'cancelled the trip'}
                {activity.activityType === 'TRIP_DELETED' && 'deleted the trip'}
                {activity.activityType === 'PARTICIPANT_INVITED' && (
                  <span>invited <span className="font-semibold text-slate-800 dark:text-slate-100">{activity.targetName}</span> to the trip</span>
                )}
                {activity.activityType === 'PARTICIPANT_JOINED' && 'joined the trip'}
                {activity.activityType === 'PARTICIPANT_DECLINED' && 'declined the trip invitation'}
                {activity.activityType === 'PARTICIPANT_LEFT' && 'left the trip'}
                {activity.activityType === 'PARTICIPANT_REMOVED' && (
                  <span>removed <span className="font-semibold text-slate-800 dark:text-slate-100">{activity.targetName}</span> from the trip</span>
                )}
                {![
                  'TRIP_CREATED', 'TRIP_UPDATED', 'TRIP_STARTED', 'TRIP_COMPLETED',
                  'TRIP_CANCELLED', 'TRIP_DELETED', 'PARTICIPANT_INVITED', 'PARTICIPANT_JOINED',
                  'PARTICIPANT_DECLINED', 'PARTICIPANT_LEFT', 'PARTICIPANT_REMOVED'
                ].includes(activity.activityType) && activity.message}
              </p>
              <span className="text-[10px] text-slate-400 dark:text-slate-500 font-medium block mt-0.5">
                {formatDate(activity.occurredAt)}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
export default TripTimeline;
