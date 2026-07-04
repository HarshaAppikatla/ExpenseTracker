import React from 'react';

interface AnalyticsLayoutProps {
  title: string;
  subtitle?: string;
  filters?: React.ReactNode;
  children: React.ReactNode;
}

export const AnalyticsLayout: React.FC<AnalyticsLayoutProps> = ({
  title,
  subtitle,
  filters,
  children,
}) => {
  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">{title}</h1>
          {subtitle && <p className="text-xs text-slate-400 mt-1">{subtitle}</p>}
        </div>
        {filters && <div className="flex items-center gap-3 self-start md:self-auto">{filters}</div>}
      </div>
      <div className="w-full">{children}</div>
    </div>
  );
};
