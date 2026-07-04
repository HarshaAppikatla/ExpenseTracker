import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, User, Settings, LogOut, FolderTree, Compass, X, Tags, TrendingUp, TrendingDown, ArrowLeftRight, Target, PiggyBank, Repeat2, Bell, BarChart3 } from 'lucide-react';
import { APP_NAME } from '@/core/constants';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useUnreadCount } from '@/features/notification/hooks/useNotification';

interface SidebarProps {
  isOpen: boolean;
  setIsOpen: (val: boolean) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen, setIsOpen }) => {
  const { logout } = useAuthContext();
  const { data: unreadCount = 0 } = useUnreadCount();

  const links = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/expenses', label: 'Expenses', icon: TrendingDown },
    { to: '/income', label: 'Income', icon: TrendingUp },
    { to: '/categories', label: 'Categories', icon: Tags },
    { to: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
    { to: '/budget', label: 'Budget', icon: Target },
    { to: '/savings', label: 'Savings', icon: PiggyBank },
    { to: '/recurring', label: 'Recurring', icon: Repeat2 },
    { to: '/notifications', label: 'Notifications', icon: Bell, badge: unreadCount },
    { to: '/insights', label: 'Insights', icon: BarChart3 },
    { to: '/groups', label: 'Groups', icon: FolderTree },
    { to: '/trips', label: 'Trips', icon: Compass, disabled: true },
    { to: '/profile', label: 'Profile', icon: User },
    { to: '/settings', label: 'Settings', icon: Settings },
  ];

  return (
    <aside
      className={`fixed top-0 left-0 z-40 h-screen transition-transform duration-300 border-r ${
        isOpen ? 'translate-x-0' : '-translate-x-full'
      } lg:translate-x-0 w-[240px] bg-white dark:bg-dark-surface border-light-border dark:border-dark-border flex flex-col justify-between`}
    >
      <div>
        {/* Sidebar Header */}
        <div className="h-[56px] border-b border-light-border dark:border-dark-border px-4 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-3 min-w-0">
            <div className="w-[30px] h-[30px] rounded-full bg-primary flex items-center justify-center text-white font-bold text-[11px] shrink-0">
              EF
            </div>
            <span className="font-bold text-[15px] leading-none text-light-text dark:text-dark-text truncate">{APP_NAME}</span>
          </div>
          <button
            onClick={() => setIsOpen(false)}
            className="lg:hidden text-light-textSecondary dark:text-slate-400 p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-btn shrink-0"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Links */}
        <nav className="p-16 space-y-4">
          {links.map((link) => {
            const Icon = link.icon;
            if (link.disabled) {
              return (
                <div
                  key={link.label}
                  className="flex items-center gap-12 px-16 py-12 rounded-btn text-slate-400 dark:text-slate-600 cursor-not-allowed select-none opacity-60"
                  title="Feature coming in a future sprint"
                >
                  <Icon className="w-5 h-5" />
                  <span className="font-medium text-sm">{link.label}</span>
                </div>
              );
            }
            return (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={() => setIsOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-12 px-16 py-12 rounded-btn transition-colors text-sm font-medium ${
                    isActive
                      ? 'bg-primary/10 text-primary'
                      : 'text-light-textSecondary dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 hover:text-light-text dark:hover:text-dark-text'
                  }`
                }
              >
                <Icon className="w-5 h-5" />
                <span className="flex-1">{link.label}</span>
                {(link as any).badge > 0 && (
                  <span className="ml-auto min-w-[18px] h-[18px] flex items-center justify-center rounded-full bg-red-500 text-white text-[10px] font-bold px-1">
                    {(link as any).badge > 99 ? '99+' : (link as any).badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Sidebar Footer */}
      <div className="p-16 border-t border-light-border dark:border-dark-border">
        <button
          onClick={logout}
          className="w-full flex items-center gap-12 px-16 py-12 rounded-btn text-light-textSecondary dark:text-slate-400 hover:bg-red-50 dark:hover:bg-red-950/20 hover:text-red-600 dark:hover:text-red-400 transition-colors text-sm font-medium focus:outline-none"
        >
          <LogOut className="w-5 h-5" />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};
