import React from 'react';
import { Wifi, WifiOff, Activity, ShieldAlert, Cpu } from 'lucide-react';
import { Card } from '../ui/Card';
import { HealthDetails } from '@/features/health/services/healthService';

interface StatusCardProps {
  status: 'online' | 'offline';
  details?: HealthDetails;
}

export const StatusCard: React.FC<StatusCardProps> = ({ status, details }) => {
  const isOnline = status === 'online';

  return (
    <Card className="hover:shadow-md transition-shadow">
      <div className="flex items-center justify-between border-b border-light-border dark:border-dark-border pb-12 mb-12">
        <div className="flex items-center gap-8">
          <Activity className="w-5 h-5 text-primary" />
          <span className="font-bold text-sm text-light-text dark:text-dark-text">System Integration</span>
        </div>
        <div className={`flex items-center gap-6 px-10 py-4 rounded-full text-xs font-semibold ${
          isOnline
            ? 'bg-green-100 text-green-700 dark:bg-green-950/30 dark:text-green-400'
            : 'bg-red-100 text-red-700 dark:bg-red-950/30 dark:text-red-400'
        }`}>
          {isOnline ? (
            <>
              <Wifi className="w-3.5 h-3.5" />
              <span>Online</span>
            </>
          ) : (
            <>
              <WifiOff className="w-3.5 h-3.5" />
              <span>Offline</span>
            </>
          )}
        </div>
      </div>

      <div className="space-y-12">
        <div className="flex items-center justify-between text-xs">
          <span className="text-light-textSecondary dark:text-slate-400 flex items-center gap-4">
            <Cpu className="w-3.5 h-3.5" /> Client Engine:
          </span>
          <span className="font-semibold text-light-text dark:text-dark-text">React 19 (TypeScript)</span>
        </div>

        <div className="flex items-center justify-between text-xs">
          <span className="text-light-textSecondary dark:text-slate-400 flex items-center gap-4">
            <Activity className="w-3.5 h-3.5" /> Server Runtime:
          </span>
          <span className="font-semibold text-light-text dark:text-dark-text">
            {isOnline && details ? `${details.name} v${details.version}` : 'Unavailable'}
          </span>
        </div>

        <div className="flex items-center justify-between text-xs">
          <span className="text-light-textSecondary dark:text-slate-400 flex items-center gap-4">
            <ShieldAlert className="w-3.5 h-3.5" /> Environment:
          </span>
          <span className="font-semibold text-light-text dark:text-dark-text uppercase">
            {isOnline && details ? details.environment : 'Unknown'}
          </span>
        </div>

        {isOnline && details && (
          <div className="text-[10px] text-light-textSecondary dark:text-slate-500 text-right mt-8 pt-8 border-t border-light-border/40 dark:border-dark-border/40">
            Last Ping: {new Date(details.timestamp).toLocaleTimeString()}
          </div>
        )}
      </div>
    </Card>
  );
};
