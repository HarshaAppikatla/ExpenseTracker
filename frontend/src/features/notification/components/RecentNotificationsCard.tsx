import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useLatestNotifications, useMarkAsRead } from '../hooks/useNotification';
import { useUIStore } from '@/store/uiStore';
import { Bell, ArrowRight, Sparkles, AlertTriangle } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

export const RecentNotificationsCard: React.FC = () => {
  const navigate = useNavigate();
  const { toggleNotificationDrawer } = useUIStore();
  const { data: notifications = [], isLoading } = useLatestNotifications(4);
  const markAsRead = useMarkAsRead();

  const handleItemClick = (n: any) => {
    if (n.status === 'UNREAD') {
      markAsRead.mutate(n.id);
    }
    if (n.groupId) {
      navigate(`/groups/${n.groupId}`);
    } else if (n.tripId) {
      navigate(`/trips/${n.tripId}`);
    } else {
      navigate('/dashboard');
    }
  };

  const formatTime = (dateString: string) => {
    try {
      return formatDistanceToNow(new Date(dateString), { addSuffix: true });
    } catch {
      return new Date(dateString).toLocaleDateString();
    }
  };

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex flex-col h-[320px] transition-all duration-300 hover:shadow-md">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <div className="p-2 bg-blue-50 dark:bg-blue-950/20 rounded-lg text-blue-600 dark:text-blue-400">
            <Bell className="w-4 h-4 animate-pulse" />
          </div>
          <h3 className="font-bold text-sm text-slate-850 dark:text-white">Recent Updates</h3>
        </div>
        <button
          onClick={() => toggleNotificationDrawer(true)}
          className="text-xs text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 font-semibold flex items-center gap-1 transition-colors"
        >
          <span>View All</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto space-y-3 pr-1">
        {isLoading ? (
          Array.from({ length: 3 }).map((_, idx) => (
            <div key={idx} className="flex gap-3 items-center animate-pulse">
              <div className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-800" />
              <div className="flex-1 space-y-1.5">
                <div className="h-3 bg-slate-100 dark:bg-slate-800 rounded w-3/4" />
                <div className="h-2 bg-slate-100 dark:bg-slate-800 rounded w-1/2" />
              </div>
            </div>
          ))
        ) : notifications.length > 0 ? (
          notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => handleItemClick(n)}
              className="group flex gap-3 items-start p-2 hover:bg-slate-50 dark:hover:bg-slate-850/50 rounded-xl cursor-pointer transition-all duration-200"
            >
              <div className={`w-2 h-2 mt-2 rounded-full flex-shrink-0 ${n.status === 'UNREAD' ? 'bg-blue-600 animate-pulse' : 'bg-slate-200 dark:bg-slate-700'}`} />
              <div className="flex-1 min-w-0">
                <p className={`text-xs text-slate-800 dark:text-slate-250 truncate ${n.status === 'UNREAD' ? 'font-bold text-slate-900 dark:text-white' : ''}`}>
                  {n.title}
                </p>
                <p className="text-[10px] text-slate-500 dark:text-slate-400 line-clamp-1 mt-0.5">
                  {n.message}
                </p>
                <span className="text-[9px] text-slate-400 block mt-1">
                  {formatTime(n.createdAt)}
                </span>
              </div>
            </div>
          ))
        ) : (
          <div className="flex flex-col items-center justify-center h-full text-center py-6">
            <Sparkles className="w-8 h-8 text-slate-300 dark:text-slate-650 mb-2" />
            <p className="text-xs text-slate-500 font-semibold">Nothing to show yet</p>
            <p className="text-[10px] text-slate-400 mt-0.5">Your updates will appear here.</p>
          </div>
        )}
      </div>
    </div>
  );
};
