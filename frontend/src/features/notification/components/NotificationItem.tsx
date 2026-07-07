import React from 'react';
import { useNavigate } from 'react-router-dom';
import { formatDistanceToNow } from 'date-fns'; // date-fns is commonly used or we can use custom format
import { 
  Bell, 
  Users, 
  MapPin, 
  DollarSign, 
  Receipt, 
  Check, 
  Trash2, 
  AlertTriangle, 
  AlertOctagon, 
  Info 
} from 'lucide-react';
import { IconButton, Tooltip } from '@mui/material';
import { Notification } from '../types';
import { useMarkAsRead, useArchiveNotification } from '../hooks/useNotification';

interface NotificationItemProps {
  notification: Notification;
  onCloseDrawer?: () => void;
}

export const NotificationItem: React.FC<NotificationItemProps> = ({ 
  notification, 
  onCloseDrawer 
}) => {
  const navigate = useNavigate();
  const markAsReadMutation = useMarkAsRead();
  const archiveMutation = useArchiveNotification();

  const handleItemClick = () => {
    // Mark as read first if unread
    if (notification.status === 'UNREAD') {
      markAsReadMutation.mutate(notification.id);
    }

    // Close drawer
    if (onCloseDrawer) {
      onCloseDrawer();
    }

    // Navigate to appropriate feature context
    if (notification.groupId) {
      navigate(`/groups/${notification.groupId}`);
    } else if (notification.tripId) {
      navigate(`/trips/${notification.tripId}`);
    } else {
      navigate('/dashboard');
    }
  };

  // Helper to resolve category icons
  const getCategoryIcon = () => {
    switch (notification.category) {
      case 'GROUP':
        return <Users className="w-5 h-5 text-indigo-500" />;
      case 'TRIP':
        return <MapPin className="w-5 h-5 text-emerald-500" />;
      case 'EXPENSE':
        return <Receipt className="w-5 h-5 text-amber-500" />;
      case 'SETTLEMENT':
        return <DollarSign className="w-5 h-5 text-teal-500" />;
      default:
        return <Bell className="w-5 h-5 text-blue-500" />;
    }
  };

  // Helper to resolve priority styles
  const getPriorityStyle = () => {
    switch (notification.priority) {
      case 'CRITICAL':
        return {
          borderClass: 'border-l-4 border-red-500',
          bgClass: 'bg-red-500/5 hover:bg-red-500/10',
          badgeColor: 'bg-red-100 text-red-700',
          icon: <AlertOctagon className="w-4 h-4 text-red-500" />
        };
      case 'HIGH':
        return {
          borderClass: 'border-l-4 border-amber-500',
          bgClass: 'bg-amber-500/5 hover:bg-amber-500/10',
          badgeColor: 'bg-amber-100 text-amber-700',
          icon: <AlertTriangle className="w-4 h-4 text-amber-500" />
        };
      case 'LOW':
        return {
          borderClass: 'border-l-4 border-gray-300',
          bgClass: 'bg-gray-50 hover:bg-gray-100',
          badgeColor: 'bg-gray-100 text-gray-700',
          icon: <Check className="w-4 h-4 text-gray-500" />
        };
      default:
        return {
          borderClass: 'border-l-4 border-blue-500',
          bgClass: 'bg-blue-500/5 hover:bg-blue-500/10',
          badgeColor: 'bg-blue-100 text-blue-700',
          icon: <Info className="w-4 h-4 text-blue-500" />
        };
    }
  };

  const priorityStyle = getPriorityStyle();

  // Handle format date (safely fallback if date-fns formatDistanceToNow is not loaded)
  const formatTime = (dateString: string) => {
    try {
      return formatDistanceToNow(new Date(dateString), { addSuffix: true });
    } catch {
      // Simple fallback format
      const date = new Date(dateString);
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
  };

  return (
    <div 
      className={`relative flex items-start gap-3 p-4 mb-3 rounded-lg border border-gray-100 shadow-sm transition-all duration-200 cursor-pointer ${priorityStyle.borderClass} ${priorityStyle.bgClass} ${notification.status === 'UNREAD' ? 'font-medium' : ''}`}
      onClick={handleItemClick}
    >
      {/* Category Icon indicator */}
      <div className="flex-shrink-0 p-2 bg-white rounded-full shadow-inner">
        {getCategoryIcon()}
      </div>

      {/* Main Content */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between gap-2 mb-1">
          <span className="text-sm font-semibold text-gray-900 truncate">
            {notification.title}
          </span>
          <span className="flex items-center gap-1 flex-shrink-0">
            <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-bold uppercase ${priorityStyle.badgeColor}`}>
              {notification.priority}
            </span>
          </span>
        </div>
        <p className="text-xs text-gray-600 line-clamp-2 mb-2 leading-relaxed">
          {notification.message}
        </p>
        <span className="text-[10px] text-gray-400">
          {formatTime(notification.createdAt)}
        </span>
      </div>

      {/* Controls Overlay */}
      <div className="flex flex-col gap-1 flex-shrink-0 ml-2" onClick={(e) => e.stopPropagation()}>
        {notification.status === 'UNREAD' && (
          <Tooltip title="Mark as read">
            <IconButton 
              size="small" 
              onClick={() => markAsReadMutation.mutate(notification.id)}
              className="text-gray-400 hover:text-green-500"
            >
              <Check className="w-4 h-4" />
            </IconButton>
          </Tooltip>
        )}
        <Tooltip title="Archive notification">
          <IconButton 
            size="small" 
            onClick={() => archiveMutation.mutate(notification.id)}
            className="text-gray-400 hover:text-red-500"
          >
            <Trash2 className="w-4 h-4" />
          </IconButton>
        </Tooltip>
      </div>

      {/* Unread pulsing circle in the corner */}
      {notification.status === 'UNREAD' && (
        <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-blue-500 animate-pulse" />
      )}
    </div>
  );
};
