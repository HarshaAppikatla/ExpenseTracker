import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, User, Settings, LogOut, Receipt, FolderTree, Compass, BarChart3, X } from 'lucide-react';
import { APP_NAME } from '@/core/constants';

interface SidebarProps {
  isOpen: boolean;
  setIsOpen: (val: boolean) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen, setIsOpen }) => {
  const links = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/personal', label: 'Personal', icon: Receipt, disabled: true },
    { to: '/groups', label: 'Groups', icon: FolderTree, disabled: true },
    { to: '/trips', label: 'Trips', icon: Compass, disabled: true },
    { to: '/reports', label: 'Reports', icon: BarChart3, disabled: true },
    { to: '/profile', label: 'Profile', icon: User },
    { to: '/settings', label: 'Settings', icon: Settings },
  ];

  return (
    <aside
      className={`fixed top-0 left-0 z-40 h-screen transition-transform duration-300 border-r ${
        isOpen ? 'translate-x-0' : '-translate-x-full'
      } lg:translate-x-0 w-64 bg-white dark:bg-dark-surface border-light-border dark:border-dark-border flex flex-col justify-between`}
    >
      <div>
        {/* Sidebar Header */}
        <div className="h-16 border-b border-light-border dark:border-dark-border px-24 flex items-center justify-between">
          <div className="flex items-center gap-8">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs">
              EF
            </div>
            <span className="font-bold text-base text-light-text dark:text-dark-text">{APP_NAME}</span>
          </div>
          <button
            onClick={() => setIsOpen(false)}
            className="lg:hidden text-light-textSecondary dark:text-slate-400 p-4 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-btn"
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
                <span>{link.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Sidebar Footer */}
      <div className="p-16 border-t border-light-border dark:border-dark-border">
        <NavLink
          to="/"
          className="flex items-center gap-12 px-16 py-12 rounded-btn text-light-textSecondary dark:text-slate-400 hover:bg-red-50 dark:hover:bg-red-950/20 hover:text-red-600 dark:hover:text-red-400 transition-colors text-sm font-medium"
        >
          <LogOut className="w-5 h-5" />
          <span>Exit App</span>
        </NavLink>
      </div>
    </aside>
  );
};
