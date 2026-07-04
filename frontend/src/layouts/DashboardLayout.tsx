import React, { useState, useEffect } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import { Sidebar } from '../components/layout/Sidebar';
import { Navbar } from '../components/layout/Navbar';
import { NotificationDrawer } from '../components/layout/NotificationDrawer';
import { useUIStore } from '@/store/uiStore';

export const DashboardLayout: React.FC = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { toggleNotificationDrawer } = useUIStore();

  const getPageTitle = (pathname: string) => {
    const segments = pathname.split('/').filter(Boolean);
    const last = segments[segments.length - 1];
    if (!last || last === 'dashboard') return 'Overview';
    // Capitalise first letter, strip hyphens
    return last.charAt(0).toUpperCase() + last.slice(1).replace(/-/g, ' ');
  };

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ignore if typing in editable areas
      const activeTag = document.activeElement?.tagName.toLowerCase();
      if (activeTag === 'input' || activeTag === 'textarea' || activeTag === 'select') {
        return;
      }

      const key = e.key.toLowerCase();
      if (e.key === 'Escape') {
        toggleNotificationDrawer(false);
      } else if (key === 'd') {
        navigate('/dashboard');
      } else if (key === 'e') {
        navigate('/expenses');
      } else if (key === 'b') {
        navigate('/budgets');
      } else if (key === 's') {
        navigate('/savings');
      } else if (key === 'r') {
        navigate('/recurring');
      } else if (key === 'i') {
        navigate('/income');
      } else if (key === 'n') {
        navigate('/notifications');
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [navigate, toggleNotificationDrawer]);

  return (
    <div className="min-h-screen bg-light-bg dark:bg-dark-bg transition-colors duration-300">
      {/* Sidebar */}
      <Sidebar isOpen={sidebarOpen} setIsOpen={setSidebarOpen} />

      {/* Slide-over notifications drawer */}
      <NotificationDrawer />

      {/* Main Content Area — offset matches sidebar width of 240px */}
      <div className="lg:pl-[240px] flex flex-col min-h-screen">
        <Navbar onMenuClick={() => setSidebarOpen(true)} title={getPageTitle(location.pathname)} />

        <main className="flex-1 p-24 md:p-32 max-w-7xl w-full mx-auto">
          {/* AnimatePresence enables exit animations when navigating between routes */}
          <AnimatePresence mode="wait" initial={false}>
            <Outlet key={location.pathname} />
          </AnimatePresence>
        </main>
      </div>
    </div>
  );
};

