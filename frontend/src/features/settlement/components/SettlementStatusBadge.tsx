import React from 'react';
import { SettlementStatus } from '../types';

interface SettlementStatusBadgeProps {
  status: SettlementStatus;
}

const SettlementStatusBadgeComponent: React.FC<SettlementStatusBadgeProps> = ({ status }) => {
  switch (status) {
    case 'PENDING':
      return (
        <span
          role="status"
          className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold rounded-full bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-900/40 text-amber-800 dark:text-amber-300"
        >
          <span className="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse" />
          Pending Payment
        </span>
      );
    case 'DISPUTED':
      return (
        <span
          role="status"
          className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold rounded-full bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 text-red-800 dark:text-red-300"
        >
          <span className="w-1.5 h-1.5 rounded-full bg-red-500" />
          Disputed
        </span>
      );
    case 'CONFIRMED':
      return (
        <span
          role="status"
          className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold rounded-full bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-900/40 text-emerald-800 dark:text-emerald-300"
        >
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2.5">
            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
          </svg>
          Settled
        </span>
      );
    default:
      return null;
  }
};

export const SettlementStatusBadge = React.memo(SettlementStatusBadgeComponent);
