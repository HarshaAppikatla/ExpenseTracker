import React from 'react';
import { Card } from '@/components/ui/Card';
import { useAppTheme } from '@/core/providers/AppProviders';
import { Sun, Moon, ShieldCheck } from 'lucide-react';
import { Button } from '@/components/ui/Button';

export const SettingsPage: React.FC = () => {
  const { mode, toggleTheme } = useAppTheme();

  return (
    <div className="max-w-2xl mx-auto space-y-24">
      <Card className="p-24 space-y-16">
        <h3 className="font-bold text-sm text-light-text dark:text-dark-text border-b border-light-border dark:border-dark-border pb-12">
          Display Settings
        </h3>
        <p className="text-xs text-light-textSecondary dark:text-slate-400">
          Modify the color theme scheme used throughout the ExpenseFlow console.
        </p>

        <div className="flex items-center justify-between p-16 bg-slate-50 dark:bg-slate-900 border border-light-border dark:border-dark-border rounded-card">
          <div className="space-y-4">
            <span className="font-bold text-xs text-light-text dark:text-dark-text">Toggle Theme Mode</span>
            <p className="text-[10px] text-light-textSecondary dark:text-slate-500">
              Switch between Light Mode and Dark Mode interface displays.
            </p>
          </div>

          <Button variant="outlined" onClick={toggleTheme} className="h-10 text-xs">
            {mode === 'light' ? (
              <>
                <Moon className="w-4 h-4" />
                <span>Dark Mode</span>
              </>
            ) : (
              <>
                <Sun className="w-4 h-4" />
                <span>Light Mode</span>
              </>
            )}
          </Button>
        </div>
      </Card>

      <Card className="p-24 space-y-16">
        <h3 className="font-bold text-sm text-light-text dark:text-dark-text border-b border-light-border dark:border-dark-border pb-12">
          System Context Info
        </h3>
        <ul className="space-y-8 text-xs text-light-textSecondary dark:text-slate-400">
          <li className="flex justify-between">
            <span className="font-medium">Version:</span>
            <span className="font-semibold text-light-text dark:text-dark-text">1.0.0</span>
          </li>
          <li className="flex justify-between">
            <span className="font-medium">Theme Setting:</span>
            <span className="font-semibold text-light-text dark:text-dark-text uppercase">{mode}</span>
          </li>
          <li className="flex justify-between">
            <span className="font-medium">Database Host:</span>
            <span className="font-semibold text-light-text dark:text-dark-text">MySQL dev (Flyway-mapped)</span>
          </li>
        </ul>
        <div className="flex items-start gap-8 bg-blue-50 dark:bg-slate-900 border border-blue-100 dark:border-slate-800 p-12 rounded-btn text-xs text-primary mt-16 leading-relaxed">
          <ShieldCheck className="w-4 h-4 mt-2 flex-shrink-0" />
          <span>
            System settings are local placeholders. Future sprints will include currency switches, notification parameters, and data exportation options.
          </span>
        </div>
      </Card>
    </div>
  );
};
