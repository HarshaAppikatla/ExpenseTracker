import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { Card } from '@/components/ui/Card';
import { useAppTheme } from '@/core/providers/AppProviders';
import { Sun, Moon, ShieldCheck } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { useProfile, useUpdateProfile } from '@/features/profile/hooks/useProfile';
import toast from 'react-hot-toast';

export const SettingsPage: React.FC = () => {
  const { mode, toggleTheme } = useAppTheme();
  const { data: profile } = useProfile();
  const updateProfile = useUpdateProfile();

  const [currency, setCurrency] = useState('$');
  const [openingBalance, setOpeningBalance] = useState('0');

  useEffect(() => {
    if (profile) {
      setCurrency(profile.preferredCurrency || '$');
      setOpeningBalance(profile.openingBalance.toString() || '0');
    }
  }, [profile]);

  const handleSavePreferences = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfile.mutate(
      {
        preferredCurrency: currency,
        openingBalance: parseFloat(openingBalance) || 0,
      },
      {
        onSuccess: () => {
          toast.success('Planning preferences updated successfully!');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to save preferences');
        },
      }
    );
  };

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="max-w-2xl mx-auto space-y-6">
      {/* Display Settings */}
      <Card className="p-6 space-y-4">
        <h3 className="font-bold text-sm text-light-text dark:text-dark-text border-b border-light-border dark:border-dark-border pb-3">
          Display Settings
        </h3>
        <p className="text-xs text-light-textSecondary dark:text-slate-400">
          Modify the color theme scheme used throughout the ExpenseFlow console.
        </p>

        <div className="flex items-center justify-between p-4 bg-slate-50 dark:bg-slate-900 border border-light-border dark:border-dark-border rounded-xl">
          <div className="space-y-1">
            <span className="font-bold text-xs text-light-text dark:text-dark-text">Toggle Theme Mode</span>
            <p className="text-[10px] text-light-textSecondary dark:text-slate-500">
              Switch between Light Mode and Dark Mode interface displays.
            </p>
          </div>

          <Button variant="outlined" onClick={toggleTheme} className="h-10 text-xs">
            {mode === 'light' ? (
              <>
                <Moon className="w-4 h-4 mr-2" />
                <span>Dark Mode</span>
              </>
            ) : (
              <>
                <Sun className="w-4 h-4 mr-2" />
                <span>Light Mode</span>
              </>
            )}
          </Button>
        </div>
      </Card>

      {/* Planning & Currency Settings */}
      <Card className="p-6 space-y-4">
        <h3 className="font-bold text-sm text-light-text dark:text-dark-text border-b border-light-border dark:border-dark-border pb-3">
          Planning & Currency Settings
        </h3>
        <p className="text-xs text-light-textSecondary dark:text-slate-400">
          Configure default reporting symbol and initial opening account buffers.
        </p>

        <form onSubmit={handleSavePreferences} className="space-y-4 pt-2">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-bold text-slate-700 dark:text-slate-350 block mb-1">
                Preferred Currency Symbol
              </label>
              <select
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
                className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-xs bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
              >
                <option value="₹">₹ (INR)</option>
                <option value="$">$ (USD)</option>
                <option value="€">€ (EUR)</option>
                <option value="£">£ (GBP)</option>
              </select>
            </div>
            <div>
              <label className="text-xs font-bold text-slate-700 dark:text-slate-350 block mb-1">
                Account Opening Balance
              </label>
              <input
                type="number"
                step="0.01"
                required
                value={openingBalance}
                onChange={(e) => setOpeningBalance(e.target.value)}
                className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-xs bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
              />
            </div>
          </div>
          <div className="flex justify-end pt-2">
            <Button type="submit" className="bg-primary hover:bg-primary/95 text-white" disabled={updateProfile.isPending}>
              {updateProfile.isPending ? 'Saving...' : 'Save Preferences'}
            </Button>
          </div>
        </form>
      </Card>

      {/* System Info */}
      <Card className="p-6 space-y-4">
        <h3 className="font-bold text-sm text-light-text dark:text-dark-text border-b border-light-border dark:border-dark-border pb-3">
          System Context Info
        </h3>
        <ul className="space-y-3 text-xs text-light-textSecondary dark:text-slate-400">
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
        <div className="flex items-start gap-3 bg-blue-50 dark:bg-slate-900 border border-blue-100 dark:border-slate-800 p-3 rounded-xl text-xs text-primary leading-relaxed">
          <ShieldCheck className="w-5 h-5 flex-shrink-0 mt-0.5" />
          <span>
            Display and planning settings are stored securely. Changing your default currency automatically propagates across all dashboard metrics, savings targets, and budget alerts.
          </span>
        </div>
      </Card>
    </motion.div>
  );
};
