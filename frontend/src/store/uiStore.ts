import { useState, useEffect } from 'react';

// Global state container
let sidebarCollapsed = false;
let notificationDrawerOpen = false;

const listeners = new Set<() => void>();

const emit = () => listeners.forEach((l) => l());

export const useUIStore = () => {
  const [collapsed, setCollapsed] = useState(sidebarCollapsed);
  const [drawerOpen, setDrawerOpen] = useState(notificationDrawerOpen);

  useEffect(() => {
    const handleUpdate = () => {
      setCollapsed(sidebarCollapsed);
      setDrawerOpen(notificationDrawerOpen);
    };

    listeners.add(handleUpdate);
    return () => {
      listeners.delete(handleUpdate);
    };
  }, []);

  const toggleSidebar = () => {
    sidebarCollapsed = !sidebarCollapsed;
    emit();
  };

  const toggleNotificationDrawer = (isOpen?: boolean) => {
    notificationDrawerOpen = isOpen !== undefined ? isOpen : !notificationDrawerOpen;
    emit();
  };

  return {
    sidebarCollapsed: collapsed,
    notificationDrawerOpen: drawerOpen,
    toggleSidebar,
    toggleNotificationDrawer,
  };
};
