import React from 'react';

interface AuthLayoutProps {
  children: React.ReactNode;
  title?: string;
  subtitle?: string;
}

export const AuthLayout: React.FC<AuthLayoutProps> = ({ children, title, subtitle }) => {
  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-slate-50 dark:bg-slate-950 p-4 md:p-8 relative overflow-hidden transition-colors duration-300">
      {/* Decorative Blur Orbs */}
      <div className="absolute top-[-10%] left-[-10%] w-[40vw] h-[40vw] rounded-full bg-indigo-200/40 dark:bg-indigo-900/20 blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[40vw] h-[40vw] rounded-full bg-teal-200/30 dark:bg-teal-900/10 blur-[120px] pointer-events-none" />

      <div className="w-full max-w-[480px] flex flex-col items-center z-10">
        {/* App Branding */}
        <div className="flex flex-col items-center gap-2 mb-8 text-center">
          <div className="h-12 w-12 rounded-2xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-600/30 text-white font-bold text-2xl tracking-tight">
            EF
          </div>
          {title && (
            <h1 className="text-3xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight mt-2">
              {title}
            </h1>
          )}
          {subtitle && (
            <p className="text-sm text-slate-500 dark:text-slate-400 max-w-[320px]">
              {subtitle}
            </p>
          )}
        </div>

        {/* Content Card */}
        {children}
      </div>
    </div>
  );
};
