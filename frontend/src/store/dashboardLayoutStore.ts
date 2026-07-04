/**
 * dashboardLayoutStore.ts
 * Persists dashboard widget layout preferences to localStorage.
 * Provides useLayoutStore() hook for React components.
 */

import { useState, useEffect } from 'react';

const STORAGE_KEY = 'expenseflow_dashboard_layout';

export type WidgetId =
  | 'overview'
  | 'health'
  | 'savings'
  | 'recurring'
  | 'notifications'
  | 'quick-actions'
  | 'charts';

export interface DashboardLayoutPrefs {
  hiddenWidgets: WidgetId[];
  compactMode: boolean;
  widgetOrder: WidgetId[];
}

const DEFAULT_ORDER: WidgetId[] = [
  'overview',
  'health',
  'savings',
  'recurring',
  'notifications',
  'quick-actions',
  'charts',
];

const DEFAULT_PREFS: DashboardLayoutPrefs = {
  hiddenWidgets: [],
  compactMode: false,
  widgetOrder: DEFAULT_ORDER,
};

function load(): DashboardLayoutPrefs {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return { ...DEFAULT_PREFS, ...JSON.parse(raw) };
  } catch {/* ignore */}
  return { ...DEFAULT_PREFS };
}

function persist(prefs: DashboardLayoutPrefs) {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs)); } catch {/* ignore */}
}

// Global singleton
let _prefs: DashboardLayoutPrefs = load();
const _listeners = new Set<() => void>();
const _emit = () => _listeners.forEach((l) => l());

function update(partial: Partial<DashboardLayoutPrefs>) {
  _prefs = { ..._prefs, ...partial };
  persist(_prefs);
  _emit();
}

export function useLayoutStore() {
  const [prefs, setPrefs] = useState<DashboardLayoutPrefs>(_prefs);

  useEffect(() => {
    const sync = () => setPrefs({ ..._prefs });
    _listeners.add(sync);
    return () => { _listeners.delete(sync); };
  }, []);

  const toggleWidget = (id: WidgetId) => {
    const hidden = prefs.hiddenWidgets.includes(id)
      ? prefs.hiddenWidgets.filter((w) => w !== id)
      : [...prefs.hiddenWidgets, id];
    update({ hiddenWidgets: hidden });
  };

  const setCompactMode = (on: boolean) => update({ compactMode: on });

  const reorderWidgets = (order: WidgetId[]) => update({ widgetOrder: order });

  const resetLayout = () => {
    update({ ...DEFAULT_PREFS });
  };

  const isHidden = (id: WidgetId) => prefs.hiddenWidgets.includes(id);

  return {
    ...prefs,
    toggleWidget,
    setCompactMode,
    reorderWidgets,
    resetLayout,
    isHidden,
  };
}
