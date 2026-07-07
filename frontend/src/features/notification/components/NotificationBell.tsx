import React, { useState } from 'react';
import { IconButton, Badge } from '@mui/material';
import { Bell } from 'lucide-react';
import { useUnreadCount } from '../hooks/useNotification';
import { NotificationDrawer } from './NotificationDrawer';

export const NotificationBell: React.FC = () => {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const { data: unreadCount = 0 } = useUnreadCount();

  const handleOpenDrawer = () => {
    setIsDrawerOpen(true);
  };

  const handleCloseDrawer = () => {
    setIsDrawerOpen(false);
  };

  return (
    <>
      <IconButton 
        onClick={handleOpenDrawer}
        color="inherit"
        className="relative hover:bg-gray-100 transition-colors"
        aria-label="View notifications"
      >
        <Badge 
          badgeContent={unreadCount} 
          color="error"
          max={99}
          slotProps={{
            badge: {
              className: `${unreadCount > 0 ? 'animate-bounce shadow-md font-bold' : ''}`
            }
          }}
        >
          <Bell className={`w-5 h-5 text-gray-700 ${unreadCount > 0 ? 'text-blue-600' : ''}`} />
        </Badge>
        {unreadCount > 0 && (
          <span className="absolute top-1.5 right-1.5 flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
          </span>
        )}
      </IconButton>
      
      <NotificationDrawer 
        open={isDrawerOpen} 
        onClose={handleCloseDrawer} 
      />
    </>
  );
};
