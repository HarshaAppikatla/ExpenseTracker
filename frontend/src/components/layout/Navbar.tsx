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
    <header className="h-[72px] bg-white dark:bg-slate-900 border-b border-[#EAECEF] dark:border-slate-800 px-[24px] flex items-center justify-between sticky top-0 z-30 transition-all duration-300">
      <div className="flex items-center gap-[12px]">
        <button
          onClick={onMenuClick}
          className="lg:hidden text-slate-500 dark:text-slate-400 p-[6px] hover:bg-slate-100 dark:hover:bg-slate-800 rounded-[10px] transition-colors"
          aria-label="Open navigation menu"
        >
          <Menu className="w-[20px] h-[20px]" />
        </button>
        <h1 className="font-bold text-[18px] text-slate-900 dark:text-slate-50 tracking-tight capitalize">
          {title}
        </h1>
      </div>

      <div className="flex items-center gap-[16px]">
        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          className="text-slate-500 dark:text-slate-400 p-[6px] hover:bg-slate-100 dark:hover:bg-slate-800 rounded-[10px] transition-colors"
          aria-label="Toggle dark mode"
        >
          {mode === 'light' ? <Moon className="w-[20px] h-[20px]" /> : <Sun className="w-[20px] h-[20px]" />}
        </button>

        {/* Notification Bell */}
        <button
          onClick={() => toggleNotificationDrawer(true)}
          className="text-slate-500 dark:text-slate-400 p-[6px] hover:bg-slate-100 dark:hover:bg-slate-800 rounded-[10px] transition-colors relative"
          aria-label="Open notifications drawer"
        >
          <Bell className="w-[20px] h-[20px]" />
          {unreadCount > 0 && (
            <span className="absolute -top-[2px] -right-[2px] min-w-[16px] h-[16px] flex items-center justify-center rounded-full bg-blue-600 text-white text-[9px] font-bold px-[4px] shadow-sm">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </button>

        {/* User Profile */}
        <button
          onClick={() => navigate('/profile')}
          className="flex items-center hover:opacity-85 transition-opacity focus:outline-none"
          aria-label="Go to profile"
        >
          <Avatar 
            name={user?.fullName || 'User'} 
            size="sm" 
            className="w-[32px] h-[32px] rounded-full border border-blue-600/10 hover:border-blue-600/30 transition-all font-bold" 
          />
        </button>
      </div>
    </header>
  );
};
