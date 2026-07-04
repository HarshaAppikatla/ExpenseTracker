import React from 'react';
import { Menu, Sun, Moon, Bell } from 'lucide-react';
import { useAppTheme } from '@/core/providers/AppProviders';
import { Avatar } from '../ui/Avatar';
import { useNavigate } from 'react-router-dom';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useUIStore } from '@/store/uiStore';
import { useUnreadCount } from '@/features/notification/hooks/useNotification';

interface NavbarProps {
  onMenuClick: () => void;
  title: string;
}

export const Navbar: React.FC<NavbarProps> = ({ onMenuClick, title }) => {
  const { mode, toggleTheme } = useAppTheme();
  const { user } = useAuthContext();
  const navigate = useNavigate();
  const { toggleNotificationDrawer } = useUIStore();
  const { data: unreadCount = 0 } = useUnreadCount();

  return (
    <header className="h-[56px] bg-white dark:bg-dark-surface border-b border-light-border dark:border-dark-border px-4 flex items-center justify-between sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <button
          onClick={onMenuClick}
          className="lg:hidden text-light-textSecondary dark:text-slate-400 p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-btn transition-colors"
          aria-label="Open navigation menu"
        >
          <Menu className="w-5 h-5" />
        </button>
        <h1 className="font-bold text-[15px] text-light-text dark:text-dark-text capitalize">
          {title}
        </h1>
      </div>

      <div className="flex items-center gap-3">
        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          className="text-light-textSecondary dark:text-slate-400 p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-btn transition-colors"
          aria-label="Toggle dark mode"
        >
          {mode === 'light' ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
        </button>

        {/* Notification Bell */}
        <button
          onClick={() => toggleNotificationDrawer(true)}
          className="text-light-textSecondary dark:text-slate-400 p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-btn transition-colors relative"
          aria-label="Open notifications drawer"
        >
          <Bell className="w-5 h-5" />
          {unreadCount > 0 && (
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full animate-pulse" />
          )}
        </button>

        {/* User Profile */}
        <button
          onClick={() => navigate('/profile')}
          className="flex items-center hover:opacity-80 transition-opacity focus:outline-none"
          aria-label="Go to profile"
        >
          <Avatar name={user?.fullName || 'User'} size="sm" />
        </button>
      </div>
    </header>
  );
};
