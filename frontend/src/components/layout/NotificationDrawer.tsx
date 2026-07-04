import React from 'react';
import { useNavigate } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { useUIStore } from '@/store/uiStore';
import { useNotifications, useMarkAsRead } from '@/features/notification/hooks/useNotification';
import { X, Check, Bell, AlertTriangle, ArrowRight } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { drawerVariants, overlayVariants } from '@/animations/variants';

export const NotificationDrawer: React.FC = () => {
  const navigate = useNavigate();
  const { notificationDrawerOpen, toggleNotificationDrawer } = useUIStore();
  const { data: list = [], isLoading } = useNotifications();
  const markAsRead = useMarkAsRead();

  const unreadList = list.filter((n) => n.status === 'UNREAD');

  const handleMarkRead = (id: string) => {
    markAsRead.mutate(id);
  };

  return (
    <AnimatePresence>
      {notificationDrawerOpen && (
        <div
          className="fixed inset-0 z-50 overflow-hidden flex justify-end"
          role="dialog"
          aria-modal="true"
          aria-label="Notifications drawer"
        >
          {/* Backdrop */}
          <motion.div
            variants={overlayVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            onClick={() => toggleNotificationDrawer(false)}
            className="absolute inset-0 bg-black/40 backdrop-blur-sm"
          />

          {/* Drawer Panel */}
          <motion.div
            variants={drawerVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="relative w-full max-w-sm bg-white dark:bg-dark-surface h-full shadow-2xl flex flex-col z-10 border-l border-light-border dark:border-dark-border"
          >
            {/* Drawer Header */}
            <div className="p-16 border-b border-light-border dark:border-dark-border flex items-center justify-between shrink-0">
              <div className="flex items-center gap-2">
                <Bell className="w-4 h-4 text-primary" aria-hidden="true" />
                <h2 className="font-bold text-sm text-slate-900 dark:text-white" id="notification-drawer-title">
                  Notifications
                </h2>
                {unreadList.length > 0 && (
                  <span
                    className="bg-red-500 text-white text-[10px] font-bold px-2 py-0.5 rounded-full"
                    aria-label={`${unreadList.length} unread`}
                  >
                    {unreadList.length}
                  </span>
                )}
              </div>
              <button
                onClick={() => toggleNotificationDrawer(false)}
                className="p-1.5 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/40"
                aria-label="Close notifications"
                // eslint-disable-next-line jsx-a11y/no-autofocus
                autoFocus
              >
                <X className="w-4 h-4" aria-hidden="true" />
              </button>
            </div>

            {/* Drawer List */}
            <div className="flex-1 overflow-y-auto p-16 space-y-4" role="list" aria-label="Unread notifications">
              {isLoading ? (
                <div className="text-center py-12 text-xs text-slate-400" aria-live="polite">Loading alerts...</div>
              ) : unreadList.length === 0 ? (
                <div className="text-center py-16 space-y-3" aria-live="polite">
                  <div className="w-12 h-12 rounded-full bg-slate-50 dark:bg-slate-900 flex items-center justify-center mx-auto border border-light-border dark:border-dark-border">
                    <Check className="w-5 h-5 text-emerald-500" aria-hidden="true" />
                  </div>
                  <p className="text-xs text-slate-500 font-semibold">You're all caught up!</p>
                  <p className="text-[10px] text-slate-400">No new alerts to review.</p>
                </div>
              ) : (
                <div className="flex flex-col gap-3">
                  {unreadList.map((n) => (
                    <div
                      key={n.id}
                      role="listitem"
                      className="bg-slate-50 dark:bg-slate-900/50 p-4 border border-light-border dark:border-slate-800 rounded-xl relative group"
                    >
                      <div className="flex items-start gap-3">
                        <div
                          className="w-7 h-7 rounded-lg bg-rose-50 dark:bg-rose-950/20 text-rose-500 border border-rose-100 dark:border-rose-950/40 flex items-center justify-center shrink-0 mt-0.5"
                          aria-hidden="true"
                        >
                          <AlertTriangle size={14} />
                        </div>
                        <div className="flex-1 min-w-0 pr-6">
                          <h4 className="text-xs font-bold text-slate-900 dark:text-white truncate">{n.title}</h4>
                          <p className="text-[10px] text-slate-500 dark:text-slate-400 leading-normal mt-0.5">
                            {n.message}
                          </p>
                          <time
                            className="text-[9px] text-slate-400 block mt-1.5 font-medium"
                            dateTime={n.createdAt}
                          >
                            {new Date(n.createdAt).toLocaleDateString()}
                          </time>
                        </div>
                      </div>

                      {/* Inline Mark Read action */}
                      <button
                        onClick={() => handleMarkRead(n.id)}
                        aria-label={`Mark "${n.title}" as read`}
                        className="absolute top-3 right-3 p-1.5 rounded-lg bg-white dark:bg-dark-surface border border-light-border dark:border-slate-850 hover:bg-emerald-50 hover:text-emerald-600 dark:hover:bg-emerald-950/20 text-slate-400 shadow-sm transition-colors focus:outline-none focus:ring-2 focus:ring-emerald-400/40"
                      >
                        <Check className="w-3.5 h-3.5" aria-hidden="true" />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Drawer Footer */}
            <div className="p-16 border-t border-light-border dark:border-dark-border bg-slate-50 dark:bg-slate-900/50 shrink-0">
              <Button
                onClick={() => {
                  toggleNotificationDrawer(false);
                  navigate('/notifications');
                }}
                className="w-full bg-primary hover:bg-primary/95 text-white flex items-center justify-center gap-2"
                aria-label="View all notifications"
              >
                <span>View All Alerts</span>
                <ArrowRight className="w-4 h-4" aria-hidden="true" />
              </Button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

