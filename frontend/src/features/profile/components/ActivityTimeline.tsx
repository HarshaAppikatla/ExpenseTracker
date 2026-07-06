import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, CheckCircle2, ShieldCheck, UserPlus } from 'lucide-react';
import { TimelineItem } from './TimelineItem';

interface ActivityTimelineProps {
  createdAt?: string;
  emailVerified?: boolean;
}

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({
  createdAt = '',
  emailVerified = true,
}) => {
  const formattedCreatedDate = createdAt
    ? new Date(createdAt).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      }) + ' \u2022 ' + new Date(createdAt).toLocaleTimeString(undefined, {
        hour: '2-digit',
        minute: '2-digit',
      })
    : 'Jul 5, 2026 \u2022 10:00 AM';

  // Current session login timestamp representation
  const loginTime = new Date().toLocaleTimeString(undefined, {
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className="bg-white dark:bg-slate-900 border border-[#EAECEF] dark:border-slate-800 rounded-[20px] p-[24px] shadow-sm flex flex-col justify-between min-h-[380px] w-full">
      <div>
        {/* Header */}
        <div className="flex justify-between items-center pb-[16px] border-b border-slate-100 dark:border-slate-800/80">
          <h2 className="text-[14px] font-bold text-slate-900 dark:text-slate-50">
            Activity Overview
          </h2>
          <Link
            to="/activity"
            className="text-[12px] font-bold text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center gap-[4px] transition-colors"
          >
            View All Activity
            <ArrowRight className="w-[14px] h-[14px]" />
          </Link>
        </div>

        {/* Timeline Items List */}
        <div className="mt-[24px] pl-[8px] space-y-[24px]">
          <TimelineItem
            title="Logged in successfully"
            timestamp={`Today, ${loginTime}`}
            icon={CheckCircle2}
            iconBgClass="bg-emerald-50 text-emerald-600 dark:bg-emerald-950/20 dark:text-emerald-400"
            iconColorClass="text-emerald-600"
            badgeText="Active Session"
          />

          {emailVerified && (
            <TimelineItem
              title="Email verified"
              timestamp="Verified at registration"
              icon={ShieldCheck}
              iconBgClass="bg-amber-50 text-amber-600 dark:bg-amber-950/20 dark:text-amber-400"
              iconColorClass="text-amber-600"
            />
          )}

          <TimelineItem
            title="Account created"
            timestamp={formattedCreatedDate}
            icon={UserPlus}
            iconBgClass="bg-purple-50 text-purple-655 dark:bg-purple-950/20 dark:text-purple-400"
            iconColorClass="text-purple-600"
            isLast={true}
          />
        </div>
      </div>
    </div>
  );
};

export default ActivityTimeline;
