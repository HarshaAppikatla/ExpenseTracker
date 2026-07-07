import React, { useState } from 'react';
import { 
  Drawer, 
  IconButton, 
  Button, 
  Skeleton, 
  Divider 
} from '@mui/material';
import { X, CheckCheck, Inbox, ChevronLeft, ChevronRight } from 'lucide-react';
import { useNotifications, useMarkAllAsRead, useUnreadCount } from '../hooks/useNotification';
import { NotificationItem } from './NotificationItem';

interface NotificationDrawerProps {
  open: boolean;
  onClose: () => void;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({ 
  open, 
  onClose 
}) => {
  const [page, setPage] = useState(0);
  const { data: pageData, isLoading } = useNotifications(page, 10);
  const { data: unreadCount = 0 } = useUnreadCount();
  const markAllAsReadMutation = useMarkAllAsRead();

  const handleMarkAllRead = () => {
    markAllAsReadMutation.mutate();
  };

  const handlePrevPage = () => {
    setPage((prev) => Math.max(0, prev - 1));
  };

  const handleNextPage = () => {
    if (pageData && !pageData.last) {
      setPage((prev) => prev + 1);
    }
  };

  const notifications = pageData?.content || [];
  const totalPages = pageData?.totalPages || 0;

  return (
    <Drawer 
      anchor="right" 
      open={open} 
      onClose={onClose}
      PaperProps={{
        className: "w-full max-w-md bg-white flex flex-col h-full shadow-2xl"
      }}
    >
      {/* Drawer Header */}
      <div className="flex items-center justify-between p-4 border-b border-gray-100">
        <div className="flex items-center gap-2">
          <h2 className="text-lg font-bold text-gray-900">Notifications</h2>
          {unreadCount > 0 && (
            <span className="px-2 py-0.5 text-xs font-bold text-white bg-blue-500 rounded-full">
              {unreadCount} unread
            </span>
          )}
        </div>
        <div className="flex items-center gap-1">
          {unreadCount > 0 && (
            <Button 
              size="small" 
              startIcon={<CheckCheck className="w-4 h-4" />}
              onClick={handleMarkAllRead}
              className="text-xs text-blue-600 hover:bg-blue-50"
            >
              Mark all read
            </Button>
          )}
          <IconButton onClick={onClose} size="small" className="text-gray-500 hover:text-gray-700">
            <X className="w-5 h-5" />
          </IconButton>
        </div>
      </div>

      {/* Drawer Body / Scrollable Feed */}
      <div className="flex-1 overflow-y-auto p-4">
        {isLoading ? (
          // Skeleton Loader
          Array.from({ length: 4 }).map((_, idx) => (
            <div key={idx} className="p-4 mb-3 border border-gray-100 rounded-lg bg-gray-50">
              <div className="flex items-center justify-between gap-4 mb-2">
                <Skeleton variant="text" width="60%" height={20} />
                <Skeleton variant="rectangular" width={40} height={16} rx={8} />
              </div>
              <Skeleton variant="text" width="100%" height={16} className="mb-2" />
              <Skeleton variant="text" width="30%" height={12} />
            </div>
          ))
        ) : notifications.length > 0 ? (
          notifications.map((notification) => (
            <NotificationItem 
              key={notification.id} 
              notification={notification} 
              onCloseDrawer={onClose}
            />
          ))
        ) : (
          // Empty State
          <div className="flex flex-col items-center justify-center h-64 text-center">
            <div className="p-4 mb-3 bg-gray-50 rounded-full">
              <Inbox className="w-10 h-10 text-gray-400" />
            </div>
            <p className="text-sm font-semibold text-gray-900">All caught up!</p>
            <p className="max-w-[240px] mt-1 text-xs text-gray-500 leading-normal">
              You don't have any notifications at the moment.
            </p>
          </div>
        )}
      </div>

      {/* Drawer Footer / Pagination */}
      {totalPages > 1 && (
        <div className="p-4 bg-gray-50 border-t border-gray-100 flex items-center justify-between">
          <span className="text-xs text-gray-500">
            Page {page + 1} of {totalPages}
          </span>
          <div className="flex items-center gap-1">
            <IconButton 
              size="small" 
              onClick={handlePrevPage} 
              disabled={page === 0}
              className={`${page === 0 ? 'text-gray-300' : 'text-gray-700 hover:bg-gray-200'}`}
            >
              <ChevronLeft className="w-5 h-5" />
            </IconButton>
            <IconButton 
              size="small" 
              onClick={handleNextPage} 
              disabled={pageData?.last}
              className={`${pageData?.last ? 'text-gray-300' : 'text-gray-700 hover:bg-gray-200'}`}
            >
              <ChevronRight className="w-5 h-5" />
            </IconButton>
          </div>
        </div>
      )}
    </Drawer>
  );
};
