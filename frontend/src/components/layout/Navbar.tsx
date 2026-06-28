import React from 'react';
import { Menu, Sun, Moon } from 'lucide-react';
import { useAppTheme } from '@/core/providers/AppProviders';
import { Avatar } from '../ui/Avatar';
import { useNavigate } from 'react-router-dom';

interface NavbarProps {
  onMenuClick: () => void;
  title: string;
}

export const Navbar: React.FC<NavbarProps> = ({ onMenuClick, title }) => {
  const { mode, toggleTheme } = useAppTheme();
  const navigate = useNavigate();

  return (
    <header className="h-16 bg-white dark:bg-dark-surface border-b border-light-border dark:border-dark-border px-24 flex items-center justify-between sticky top-0 z-30">
      <div className="flex items-center gap-16">
        <button
          onClick={onMenuClick}
          className="lg:hidden text-light-textSecondary dark:text-slate-400 p-8 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-btn transition-colors"
          aria-label="Open navigation menu"
        >
          <Menu className="w-5 h-5" />
        </button>
        <h1 className="font-bold text-base text-light-text dark:text-dark-text capitalize">
          {title}
        </h1>
      </div>

      <div className="flex items-center gap-16">
        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          className="text-light-textSecondary dark:text-slate-400 p-8 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-btn transition-colors"
          aria-label="Toggle dark mode"
        >
          {mode === 'light' ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
        </button>

        {/* User Profile */}
        <button
          onClick={() => navigate('/profile')}
          className="flex items-center hover:opacity-80 transition-opacity focus:outline-none"
          aria-label="Go to profile"
        >
          <Avatar name="Jane Doe" size="sm" />
        </button>
      </div>
    </header>
  );
};
