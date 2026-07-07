import React, { useState, useEffect } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { useUIStore } from '@/store/uiStore';
import { 
  useNotifications, 
  useMarkAllAsRead, 
  useUnreadCount 
} from '@/features/notification/hooks/useNotification';
import { X, CheckCheck, Inbox, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { drawerVariants, overlayVariants } from '@/animations/variants';
import { NotificationItem } from '@/features/notification/components/NotificationItem';
import { IconButton } from '@mui/material';

export const NotificationDrawer: React.FC = () => {
  const { notificationDrawerOpen, toggleNotificationDrawer } = useUIStore();
  const [page, setPage] = useState(0);
  const { data: pageData, isLoading } = useNotifications(page, 5); // 5 items per page in drawer
  const { data: unreadCount = 0 } = useUnreadCount();
  const markAllAsReadMutation = useMarkAllAsRead();

  // Reset page to 0 when opening drawer
  useEffect(() => {
    if (notificationDrawerOpen) {
      setPage(0);
    }
  }, [notificationDrawerOpen]);

  const handleMarkAllRead = (e: React.MouseEvent) => {
    e.stopPropagation();
    markAllAsReadMutation.mutate();
  };

  const handlePrevPage = (e: React.MouseEvent) => {
    e.stopPropagation();
    setPage((prev) => Math.max(0, prev - 1));
  };

  const handleNextPage = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (pageData && !pageData.last) {
      setPage((prev) => prev + 1);
    }
  };

  const notifications = pageData?.content || [];
  const totalPages = pageData?.totalPages || 0;

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
            <div className="p-4 border-b border-light-border dark:border-dark-border flex items-center justify-between shrink-0">
              <div className="flex items-center gap-2">
                <h2 className="font-bold text-sm text-slate-900 dark:text-white" id="notification-drawer-title">
                  Notifications
                </h2>
                {unreadCount > 0 && (
                  <span
                    className="bg-blue-600 text-white text-[10px] font-bold px-2 py-0.5 rounded-full"
                    aria-label={`${unreadCount} unread`}
                  >
                    {unreadCount}
                  </span>
                )}
              </div>
              <div className="flex items-center gap-1">
                {unreadCount > 0 && (
                  <button
                    onClick={handleMarkAllRead}
                    className="text-xs text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 font-semibold px-2 py-1 rounded-lg"
                  >
                    Mark all read
                  </button>
                )}
                <button
                  onClick={() => toggleNotificationDrawer(false)}
                  className="p-1.5 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-400 focus:outline-none"
                  aria-label="Close notifications"
                >
                  <X className="w-4 h-4" aria-hidden="true" />
                </button>
              </div>
            </div>

            {/* Drawer List */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3" role="list" aria-label="Notifications list">
              {isLoading ? (
                <div className="text-center py-12 text-xs text-slate-400" aria-live="polite">
                  Loading notifications...
                </div>
              ) : notifications.length === 0 ? (
                <div className="text-center py-16 space-y-3" aria-live="polite">
                  <div className="w-12 h-12 rounded-full bg-slate-50 dark:bg-slate-900 flex items-center justify-center mx-auto border border-light-border dark:border-dark-border">
                    <Inbox className="w-5 h-5 text-slate-400" aria-hidden="true" />
                  </div>
                  <p className="text-xs text-slate-500 font-semibold">You're all caught up!</p>
                  <p className="text-[10px] text-slate-400">No new alerts to review.</p>
                </div>
              ) : (
                notifications.map((n) => (
                  <NotificationItem 
                    key={n.id} 
                    notification={n} 
                    onCloseDrawer={() => toggleNotificationDrawer(false)}
                  />
                ))
              )}
            </div>

            {/* Drawer Footer & Pagination */}
            {totalPages > 1 && (
              <div className="p-4 bg-slate-50 dark:bg-slate-900/50 border-t border-light-border dark:border-dark-border flex items-center justify-between shrink-0">
                <span className="text-xs text-slate-500">
                  Page {page + 1} of {totalPages}
                </span>
                <div className="flex items-center gap-1">
                  <IconButton 
                    size="small" 
                    onClick={handlePrevPage} 
                    disabled={page === 0}
                    className={`${page === 0 ? 'text-gray-300' : 'text-gray-700 hover:bg-gray-200 dark:text-gray-300 dark:hover:bg-slate-800'}`}
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </IconButton>
                  <IconButton 
                    size="small" 
                    onClick={handleNextPage} 
                    disabled={pageData?.last}
                    className={`${pageData?.last ? 'text-gray-300' : 'text-gray-700 hover:bg-gray-200 dark:text-gray-300 dark:hover:bg-slate-800'}`}
                  >
                    <ChevronRight className="w-4 h-4" />
                  </IconButton>
                </div>
              </div>
            )}
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};
