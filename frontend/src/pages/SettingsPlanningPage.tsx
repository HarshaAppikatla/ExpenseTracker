import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import {
  CalendarDays,
  RefreshCcw,
  Bell,
  DollarSign,
  Wallet,
  Check,
  RotateCcw,
} from 'lucide-react';
import toast from 'react-hot-toast';

const STORAGE_KEY = 'expenseflow_planning_prefs';

interface PlanningPreferences {
  defaultBudgetLimit: string;
  defaultBudgetPeriod: 'monthly' | 'weekly' | 'yearly';
  firstDayOfWeek: 'monday' | 'sunday' | 'saturday';
  recurringDefaultFrequency: 'daily' | 'weekly' | 'monthly' | 'yearly';
  recurringDefaultNotify: boolean;
  budgetOverspendAlert: boolean;
  savingsReminderDay: string;
  lowBalanceThreshold: string;
}

const defaultPrefs: PlanningPreferences = {
  defaultBudgetLimit: '10000',
  defaultBudgetPeriod: 'monthly',
  firstDayOfWeek: 'monday',
  recurringDefaultFrequency: 'monthly',
  recurringDefaultNotify: true,
  budgetOverspendAlert: true,
  savingsReminderDay: '1',
  lowBalanceThreshold: '1000',
};

function loadPrefs(): PlanningPreferences {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return { ...defaultPrefs, ...JSON.parse(raw) };
  } catch {/* ignore */}
  return defaultPrefs;
}

export const SettingsPlanningPage: React.FC = () => {
  const [prefs, setPrefs] = useState<PlanningPreferences>(loadPrefs);
  const [saved, setSaved] = useState(false);

  const update = <K extends keyof PlanningPreferences>(key: K, value: PlanningPreferences[K]) => {
    setPrefs((p) => ({ ...p, [key]: value }));
    setSaved(false);
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs));
    setSaved(true);
    toast.success('Planning preferences saved locally!');
    setTimeout(() => setSaved(false), 2500);
  };

  const handleReset = () => {
    setPrefs(defaultPrefs);
    localStorage.removeItem(STORAGE_KEY);
    setSaved(false);
    toast('Preferences reset to defaults.', { icon: '🔄' });
  };

  const labelCls = 'text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1.5';
  const inputCls = 'w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-xs bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/40 transition-shadow';
  const toggleCls = (active: boolean) =>
    `relative inline-flex h-5 w-9 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-primary/40 ${active ? 'bg-primary' : 'bg-slate-300 dark:bg-slate-700'}`;

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="w-full">
      <form onSubmit={handleSave} className="max-w-2xl mx-auto space-y-5">
        {/* Budget Defaults */}
        <Card className="p-6 space-y-4">
          <div className="flex items-center gap-2 border-b border-light-border dark:border-dark-border pb-3">
            <Wallet className="w-4 h-4 text-primary" />
            <h3 className="font-bold text-sm text-light-text dark:text-dark-text">Budget Defaults</h3>
          </div>
          <p className="text-xs text-light-textSecondary dark:text-slate-400">
            Pre-filled values used when you create a new budget plan.
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="plan-budget-limit" className={labelCls}>Default Budget Limit</label>
              <input
                id="plan-budget-limit"
                type="number"
                step="100"
                min="0"
                value={prefs.defaultBudgetLimit}
                onChange={(e) => update('defaultBudgetLimit', e.target.value)}
                className={inputCls}
              />
            </div>
            <div>
              <label htmlFor="plan-budget-period" className={labelCls}>Default Budget Period</label>
              <select
                id="plan-budget-period"
                value={prefs.defaultBudgetPeriod}
                onChange={(e) => update('defaultBudgetPeriod', e.target.value as PlanningPreferences['defaultBudgetPeriod'])}
                className={inputCls}
              >
                <option value="weekly">Weekly</option>
                <option value="monthly">Monthly</option>
                <option value="yearly">Yearly</option>
              </select>
            </div>
          </div>
        </Card>

        {/* Calendar Preferences */}
        <Card className="p-6 space-y-4">
          <div className="flex items-center gap-2 border-b border-light-border dark:border-dark-border pb-3">
            <CalendarDays className="w-4 h-4 text-primary" />
            <h3 className="font-bold text-sm text-light-text dark:text-dark-text">Calendar Preferences</h3>
          </div>
          <p className="text-xs text-light-textSecondary dark:text-slate-400">
            Controls how weekly summaries and spending calendars are rendered.
          </p>
          <div>
            <label htmlFor="plan-first-day" className={labelCls}>First Day of Week</label>
            <select
              id="plan-first-day"
              value={prefs.firstDayOfWeek}
              onChange={(e) => update('firstDayOfWeek', e.target.value as PlanningPreferences['firstDayOfWeek'])}
              className={inputCls}
            >
              <option value="monday">Monday</option>
              <option value="sunday">Sunday</option>
              <option value="saturday">Saturday</option>
            </select>
          </div>
        </Card>

        {/* Recurring Defaults */}
        <Card className="p-6 space-y-4">
          <div className="flex items-center gap-2 border-b border-light-border dark:border-dark-border pb-3">
            <RefreshCcw className="w-4 h-4 text-primary" />
            <h3 className="font-bold text-sm text-light-text dark:text-dark-text">Recurring Transaction Defaults</h3>
          </div>
          <p className="text-xs text-light-textSecondary dark:text-slate-400">
            Pre-filled values when creating new recurring templates.
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="plan-recurring-freq" className={labelCls}>Default Frequency</label>
              <select
                id="plan-recurring-freq"
                value={prefs.recurringDefaultFrequency}
                onChange={(e) => update('recurringDefaultFrequency', e.target.value as PlanningPreferences['recurringDefaultFrequency'])}
                className={inputCls}
              >
                <option value="daily">Daily</option>
                <option value="weekly">Weekly</option>
                <option value="monthly">Monthly</option>
                <option value="yearly">Yearly</option>
              </select>
            </div>
            <div className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-900 border border-light-border dark:border-dark-border rounded-xl self-end">
              <span className="text-xs font-semibold text-slate-700 dark:text-slate-300">Notify before execution</span>
              <button
                type="button"
                role="switch"
                aria-checked={prefs.recurringDefaultNotify}
                onClick={() => update('recurringDefaultNotify', !prefs.recurringDefaultNotify)}
                className={toggleCls(prefs.recurringDefaultNotify)}
              >
                <span className={`inline-block h-3.5 w-3.5 transform rounded-full bg-white shadow transition-transform duration-200 ${prefs.recurringDefaultNotify ? 'translate-x-4' : 'translate-x-1'}`} />
              </button>
            </div>
          </div>
        </Card>

        {/* Notification Preferences */}
        <Card className="p-6 space-y-4">
          <div className="flex items-center gap-2 border-b border-light-border dark:border-dark-border pb-3">
            <Bell className="w-4 h-4 text-primary" />
            <h3 className="font-bold text-sm text-light-text dark:text-dark-text">Notification Preferences</h3>
          </div>
          <p className="text-xs text-light-textSecondary dark:text-slate-400">
            Control when alerts are triggered for your financial health.
          </p>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-900 border border-light-border dark:border-dark-border rounded-xl">
              <div>
                <span className="text-xs font-semibold text-slate-700 dark:text-slate-300 block">Budget Overspend Alert</span>
                <span className="text-[10px] text-slate-400">Get notified when you exceed a budget category</span>
              </div>
              <button
                type="button"
                role="switch"
                aria-checked={prefs.budgetOverspendAlert}
                onClick={() => update('budgetOverspendAlert', !prefs.budgetOverspendAlert)}
                className={toggleCls(prefs.budgetOverspendAlert)}
              >
                <span className={`inline-block h-3.5 w-3.5 transform rounded-full bg-white shadow transition-transform duration-200 ${prefs.budgetOverspendAlert ? 'translate-x-4' : 'translate-x-1'}`} />
              </button>
            </div>

            <div className="flex flex-col sm:flex-row sm:items-center gap-3 p-3 bg-slate-50 dark:bg-slate-900 border border-light-border dark:border-dark-border rounded-xl">
              <div className="flex-1">
                <label htmlFor="plan-low-balance" className="text-xs font-semibold text-slate-700 dark:text-slate-300 block">Low Balance Threshold</label>
                <span className="text-[10px] text-slate-400">Alert when net balance drops below this amount</span>
              </div>
              <div className="flex items-center gap-2">
                <DollarSign className="w-3.5 h-3.5 text-slate-400" />
                <input
                  id="plan-low-balance"
                  type="number"
                  min="0"
                  step="100"
                  value={prefs.lowBalanceThreshold}
                  onChange={(e) => update('lowBalanceThreshold', e.target.value)}
                  className="w-28 border border-light-border dark:border-dark-border rounded-lg px-2 py-1.5 text-xs bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/40"
                />
              </div>
            </div>

            <div className="flex flex-col sm:flex-row sm:items-center gap-3 p-3 bg-slate-50 dark:bg-slate-900 border border-light-border dark:border-dark-border rounded-xl">
              <div className="flex-1">
                <label htmlFor="plan-savings-day" className="text-xs font-semibold text-slate-700 dark:text-slate-300 block">Monthly Savings Reminder Day</label>
                <span className="text-[10px] text-slate-400">Day of month (1–28) to remind you to contribute to savings goals</span>
              </div>
              <input
                id="plan-savings-day"
                type="number"
                min="1"
                max="28"
                value={prefs.savingsReminderDay}
                onChange={(e) => update('savingsReminderDay', e.target.value)}
                className="w-20 border border-light-border dark:border-dark-border rounded-lg px-2 py-1.5 text-xs bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/40"
              />
            </div>
          </div>
        </Card>

        {/* Action Row */}
        <div className="flex items-center justify-between gap-3 pb-4">
          <button
            type="button"
            onClick={handleReset}
            className="flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 dark:hover:text-slate-100 transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Reset to defaults
          </button>
          <Button type="submit" className="bg-primary hover:bg-primary/90 text-white flex items-center gap-2">
            {saved && <Check className="w-3.5 h-3.5" />}
            {saved ? 'Saved!' : 'Save Preferences'}
          </Button>
        </div>
      </form>
    </motion.div>
  );
};
