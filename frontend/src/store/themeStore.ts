/**
 * themeStore.ts
 * Lightweight pub/sub store for theme mode — 'light' | 'dark'.
 * Syncs with localStorage and the <html> class so Tailwind dark-mode works.
 * Components read via useThemeStore().
 */

import { useState, useEffect } from 'react';
import { STORAGE_KEYS } from '@/core/constants';

type ThemeMode = 'light' | 'dark';

function resolveInitialMode(): ThemeMode {
  try {
    const saved = localStorage.getItem(STORAGE_KEYS.THEME);
    if (saved === 'light' || saved === 'dark') return saved;
    if (window.matchMedia?.('(prefers-color-scheme: dark)').matches) return 'dark';
  } catch {/* SSR / no localStorage */}
  return 'light';
}

function applyMode(mode: ThemeMode) {
  const root = window.document.documentElement;
  if (mode === 'dark') root.classList.add('dark');
  else root.classList.remove('dark');
  try { localStorage.setItem(STORAGE_KEYS.THEME, mode); } catch {/* ignore */}
}

// Global singleton
let _mode: ThemeMode = resolveInitialMode();
applyMode(_mode);

const _listeners = new Set<() => void>();
const _emit = () => _listeners.forEach((l) => l());

/** Toggle or explicitly set the theme mode globally. */
export function setThemeMode(next: ThemeMode | 'toggle') {
  _mode = next === 'toggle' ? (_mode === 'light' ? 'dark' : 'light') : next;
  applyMode(_mode);
  _emit();
}

/** React hook — returns { mode, toggleTheme, setMode } */
export function useThemeStore() {
  const [mode, setMode] = useState<ThemeMode>(_mode);

  useEffect(() => {
    const sync = () => setMode(_mode);
    _listeners.add(sync);
    return () => { _listeners.delete(sync); };
  }, []);

  return {
    mode,
    toggleTheme: () => setThemeMode('toggle'),
    setMode: (m: ThemeMode) => setThemeMode(m),
    isDark: mode === 'dark',
  };
}
