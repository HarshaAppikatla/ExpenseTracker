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
      } lg:translate-x-0 w-[240px] bg-slate-50 dark:bg-slate-900 border-[#EAECEF] dark:border-slate-800 flex flex-col justify-between`}
    >
      <div className="flex flex-col h-full overflow-y-auto pb-[72px]">
        {/* Sidebar Header */}
        <div className="h-[72px] border-b border-[#EAECEF] dark:border-slate-800 px-[24px] flex items-center justify-between shrink-0">
          <div className="flex items-center gap-[12px] min-w-0">
            <div className="w-[36px] h-[36px] rounded-[10px] bg-blue-600 flex items-center justify-center text-white font-extrabold text-[15px] shrink-0 shadow-sm">
              EF
            </div>
            <span className="font-bold text-[16px] tracking-tight text-slate-900 dark:text-slate-100 truncate">{APP_NAME}</span>
          </div>
          <button
            onClick={() => setIsOpen(false)}
            className="lg:hidden text-slate-500 dark:text-slate-400 p-[6px] hover:bg-slate-100 dark:hover:bg-slate-800 rounded-[10px] shrink-0"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Links */}
        <nav className="px-[16px] py-[24px] space-y-[4px]">
          {links.map((link) => {
            const Icon = link.icon;
            if (link.disabled) {
              return (
                <div
                  key={link.label}
                  className="flex items-center gap-[12px] px-[16px] py-[10px] rounded-[12px] text-slate-400 dark:text-slate-600 cursor-not-allowed select-none opacity-50 text-[14px] font-semibold"
                  title="Feature coming in a future sprint"
                >
                  <Icon className="w-[20px] h-[20px] shrink-0" />
                  <span>{link.label}</span>
                </div>
              );
            }
            return (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={() => setIsOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-[12px] px-[16px] py-[10px] rounded-[12px] transition-all duration-200 text-[14px] font-semibold ${
                    isActive
                      ? 'bg-blue-600/10 text-blue-600 dark:bg-blue-500/10 dark:text-blue-400'
                      : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800/60 hover:text-slate-950 dark:hover:text-slate-100'
                  }`
                }
              >
                <Icon className="w-[20px] h-[20px] shrink-0" />
                <span className="flex-1 truncate">{link.label}</span>
                {(link as any).badge > 0 && (
                  <span className="ml-auto min-w-[20px] h-[20px] flex items-center justify-center rounded-full bg-blue-600 text-white text-[10px] font-bold px-[6px] shadow-sm">
                    {(link as any).badge > 99 ? '99+' : (link as any).badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Sidebar Footer - Logout fixed at bottom */}
      <div className="absolute bottom-0 left-0 w-full p-[16px] border-t border-[#EAECEF] dark:border-slate-800 bg-slate-50 dark:bg-slate-900 z-10">
        <button
          onClick={() => logout()}
          className="w-full flex items-center gap-[12px] px-[16px] py-[10px] rounded-[12px] text-slate-600 dark:text-slate-400 hover:bg-red-50 dark:hover:bg-red-950/20 hover:text-red-600 dark:hover:text-red-400 transition-colors text-[14px] font-semibold focus:outline-none"
        >
          <LogOut className="w-[20px] h-[20px] shrink-0" />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};
