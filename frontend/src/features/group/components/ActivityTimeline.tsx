import React from 'react';
import { GroupActivityDto } from '../types/group';
import { ActivityItem } from './ActivityItem';
import { EmptyTimelineState } from './GroupEmptyStates';

interface ActivityTimelineProps {
  activities: GroupActivityDto[];
}

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({ activities }) => {
  if (!activities || activities.length === 0) {
    return <EmptyTimelineState />;
  }

  return (
    <div className="relative">
      {/* Vertical line line */}
      <div className="absolute left-4 top-4 bottom-4 w-0.5 bg-slate-100 dark:bg-slate-800 pointer-events-none"></div>

      {/* List container */}
      <div className="space-y-6">
        {activities.map((activity) => (
          <ActivityItem key={activity.id} activity={activity} />
        ))}
      </div>
    </div>
  );
};
export default ActivityTimeline;
