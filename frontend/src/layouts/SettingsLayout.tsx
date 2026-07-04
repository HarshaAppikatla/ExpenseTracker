import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { Settings, Shield, Sliders } from 'lucide-react';

export const SettingsLayout: React.FC = () => {
  const tabs = [
    { to: '/settings', end: true, label: 'General Settings', icon: Settings },
    { to: '/settings/planning', label: 'Planning Preferences', icon: Sliders },
  ];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Settings</h1>
        <p className="text-xs text-slate-400 mt-1">Configure your local client preferences and planning properties</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 items-start">
        {/* Navigation Sidebar Tabs */}
        <div className="bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-card p-4 space-y-1">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <NavLink
                key={tab.to}
                to={tab.to}
                end={tab.end}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-3 rounded-btn text-xs font-semibold transition-colors ${
                    isActive
                      ? 'bg-primary/10 text-primary'
                      : 'text-slate-500 hover:bg-slate-50 dark:hover:bg-slate-800/50 hover:text-slate-900 dark:hover:text-slate-200'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
              </NavLink>
            );
          })}
        </div>

        {/* Settings Subpages */}
        <div className="lg:col-span-3">
          <Outlet />
        </div>
      </div>
    </div>
  );
};
