import React from 'react';
import { useNotifications, useMarkAsRead, useArchiveNotification } from '@/features/notification/hooks/useNotification';
import { Notification } from '@/features/notification/services/notificationService';
import { Bell, CheckCheck, Trash2, AlertTriangle, Info, Zap, AlertCircle } from 'lucide-react';

const PRIORITY_CONFIG: Record<string, { icon: React.FC<any>; color: string; dot: string }> = {
  CRITICAL: { icon: Zap,          color: 'text-red-600 dark:text-red-400',    dot: 'bg-red-500' },
  HIGH:     { icon: AlertTriangle, color: 'text-amber-600 dark:text-amber-400', dot: 'bg-amber-500' },
  NORMAL:   { icon: Info,          color: 'text-blue-600 dark:text-blue-400',   dot: 'bg-blue-500' },
  LOW:      { icon: AlertCircle,   color: 'text-slate-500 dark:text-slate-400', dot: 'bg-slate-400' },
};

const TYPE_LABELS: Record<string, string> = {
  BUDGET_LIMIT:      'Budget Exceeded',
  BUDGET_WARNING:    'Budget Warning',
  RECURRING_EXECUTION: 'Payment Executed',
  RECURRING_FAILED:  'Payment Failed',
  SAVINGS_COMPLETED: 'Goal Reached',
};

const fmtTime = (d: string) => {
  const dt = new Date(d);
  const now = new Date();
  const diff = (now.getTime() - dt.getTime()) / 1000;
  if (diff < 60)   return 'Just now';
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
  return dt.toLocaleDateString('en-US', { day: 'numeric', month: 'short' });
};

export const NotificationsPage: React.FC = () => {
  const { data: notifications = [], isLoading } = useNotifications();
  const markRead = useMarkAsRead();
  const archive = useArchiveNotification();

  const unread = notifications.filter((n) => n.status === 'UNREAD');
  const read   = notifications.filter((n) => n.status === 'READ');

  const markAllRead = () => {
    unread.forEach((n) => markRead.mutate(n.id));
  };

  const NotificationCard = ({ n }: { n: Notification }) => {
    const cfg = PRIORITY_CONFIG[n.priority] ?? PRIORITY_CONFIG.LOW;
    const Icon = cfg.icon;
    const isUnread = n.status === 'UNREAD';
    return (
      <div
        className={`flex gap-3 p-4 rounded-2xl border transition-colors ${
          isUnread
            ? 'bg-white dark:bg-dark-surface border-light-border dark:border-dark-border shadow-sm'
            : 'bg-slate-50/60 dark:bg-slate-900/40 border-transparent'
        }`}
      >
        {/* Icon */}
        <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${isUnread ? 'bg-slate-100 dark:bg-slate-800' : 'bg-transparent'}`}>
          <Icon size={16} className={cfg.color} />
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div>
              <div className="flex items-center gap-2">
                {isUnread && <span className={`w-2 h-2 rounded-full shrink-0 ${cfg.dot}`} />}
                <span className={`text-sm font-semibold ${isUnread ? 'text-slate-800 dark:text-slate-100' : 'text-slate-600 dark:text-slate-300'}`}>
                  {n.title}
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 line-clamp-2">{n.message}</p>
            </div>
            <div className="flex items-center gap-1 shrink-0">
              {isUnread && (
                <button onClick={() => markRead.mutate(n.id)} title="Mark as read" className="p-1.5 rounded-lg hover:bg-emerald-50 dark:hover:bg-emerald-950/20 text-slate-400 hover:text-emerald-600 transition-colors">
                  <CheckCheck size={14} />
                </button>
              )}
              <button onClick={() => archive.mutate(n.id)} title="Archive" className="p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-950/20 text-slate-400 hover:text-red-500 transition-colors">
                <Trash2 size={14} />
              </button>
            </div>
          </div>
          <div className="flex items-center gap-2 mt-1.5">
            <span className="text-[10px] bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400 px-2 py-0.5 rounded-full font-medium">
              {TYPE_LABELS[n.notificationType] ?? n.notificationType}
            </span>
            <span className="text-[10px] text-slate-400">{fmtTime(n.createdAt)}</span>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Notification Center</h1>
          <p className="text-xs text-slate-400 mt-0.5">Alerts from budget limits, recurring payments, and savings milestones</p>
        </div>
        {unread.length > 0 && (
          <button
            onClick={markAllRead}
            className="inline-flex items-center gap-2 px-4 py-2 border border-light-border dark:border-dark-border text-slate-600 dark:text-slate-300 rounded-xl text-sm font-semibold hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
          >
            <CheckCheck size={16} /> Mark all read
          </button>
        )}
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Total', value: notifications.length, color: 'indigo' },
          { label: 'Unread', value: unread.length, color: 'amber' },
          { label: 'Read', value: read.length, color: 'emerald' },
        ].map((s) => (
          <div key={s.label} className="bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-2xl p-4 text-center">
            <p className={`text-2xl font-extrabold text-${s.color}-600 dark:text-${s.color}-400`}>{s.value}</p>
            <p className="text-[11px] text-slate-400 mt-0.5">{s.label}</p>
          </div>
        ))}
      </div>

      {isLoading ? (
        <div className="text-center py-16 text-slate-400">Loading notifications...</div>
      ) : notifications.length === 0 ? (
        <div className="flex flex-col items-center gap-3 py-20">
          <div className="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
            <Bell size={28} className="text-slate-400" />
          </div>
          <p className="text-slate-600 dark:text-slate-300 font-semibold">You're all caught up!</p>
          <p className="text-sm text-slate-400">Notifications appear here when budgets are hit, payments run, or goals are reached.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {unread.length > 0 && (
            <div>
              <h2 className="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wide mb-3">
                Unread — {unread.length}
              </h2>
              <div className="space-y-2">
                {unread.map((n) => <NotificationCard key={n.id} n={n} />)}
              </div>
            </div>
          )}
          {read.length > 0 && (
            <div>
              <h2 className="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wide mb-3">
                Earlier
              </h2>
              <div className="space-y-2">
                {read.map((n) => <NotificationCard key={n.id} n={n} />)}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
