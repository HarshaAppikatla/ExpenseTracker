import React from 'react';
import { SettlementResponse } from '../types';
import { SettlementStatusBadge } from './SettlementStatusBadge';
import { settlementPermissions } from '../utils/permissions';
import { GroupRole } from '../../group/types/group';

interface SettlementRowProps {
  settlement: SettlementResponse;
  currentUserId: string;
  currentUserRole: GroupRole;
  isGroupArchived: boolean;
  onConfirmPayment: (id: string) => void;
  onDisputeClick: (id: string) => void;
  onResolve: (id: string) => void;
  isMutating: boolean;
  activeMutatingId: string | null;
}

const SettlementRowComponent: React.FC<SettlementRowProps> = ({
  settlement,
  currentUserId,
  currentUserRole,
  isGroupArchived,
  onConfirmPayment,
  onDisputeClick,
  onResolve,
  isMutating,
  activeMutatingId,
}) => {
  const formatAmount = (val: number, currency: string) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency || 'INR',
    }).format(val);
  };

  const isRowMutating = isMutating && activeMutatingId === settlement.id;

  const canConfirm = settlementPermissions.canConfirm(settlement, currentUserId, isGroupArchived);
  const canDispute = settlementPermissions.canDispute(settlement, currentUserId, isGroupArchived);
  const canResolve = settlementPermissions.canResolve(settlement, currentUserRole, isGroupArchived);

  return (
    <div
      className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800/80 rounded-xl p-5 shadow-sm hover:shadow-md transition-all duration-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4"
      role="listitem"
    >
      <div className="flex items-center gap-4">
        {/* Visual Transaction Flow: Debtor -> Arrow -> Creditor */}
        <div className="flex items-center gap-2.5" aria-hidden="true">
          <div
            title={`Debtor: ${settlement.fromUserName}`}
            className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center font-bold text-slate-700 dark:text-slate-250 select-none shadow-sm"
          >
            {settlement.fromUserName.charAt(0).toUpperCase()}
          </div>
          
          <div className="flex flex-col items-center justify-center min-w-[32px]">
            <svg
              className="w-5 h-5 text-indigo-500/80 dark:text-indigo-400/80 animate-pulse"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth="2.5"
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="M17 8l4 4m0 0l-4 4m4-4H3" />
            </svg>
          </div>

          <div
            title={`Creditor: ${settlement.toUserName}`}
            className="w-10 h-10 rounded-full bg-indigo-50 dark:bg-indigo-950/30 border border-indigo-100 dark:border-indigo-900/60 flex items-center justify-center font-bold text-indigo-650 dark:text-indigo-400 select-none shadow-sm"
          >
            {settlement.toUserName.charAt(0).toUpperCase()}
          </div>
        </div>

        <div>
          <div className="text-sm font-bold text-slate-800 dark:text-slate-200">
            {settlement.fromUserName}{' '}
            <span className="font-normal text-slate-400 text-xs">owes</span>{' '}
            {settlement.toUserName}
          </div>
          <div className="flex flex-wrap items-center gap-2.5 mt-1">
            <span className="text-sm font-extrabold text-slate-900 dark:text-white tracking-tight">
              {formatAmount(settlement.amount, settlement.currency)}
            </span>
            <SettlementStatusBadge status={settlement.status} />
          </div>
        </div>
      </div>

      {/* Action Controls */}
      <div className="flex flex-wrap gap-2 items-center sm:justify-end">
        {canConfirm && (
          <button
            onClick={() => onConfirmPayment(settlement.id)}
            disabled={isMutating}
            aria-label={`Confirm payment of ${formatAmount(settlement.amount, settlement.currency)} to ${settlement.toUserName}`}
            className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-sm hover:shadow-emerald-600/10 transition-all disabled:opacity-50 active:scale-98"
          >
            {isRowMutating ? 'Confirming...' : 'Confirm Payment'}
          </button>
        )}

        {canDispute && (
          <button
            onClick={() => onDisputeClick(settlement.id)}
            disabled={isMutating}
            aria-label={`Dispute payment of ${formatAmount(settlement.amount, settlement.currency)} from ${settlement.fromUserName}`}
            className="px-3.5 py-1.5 bg-red-50 hover:bg-red-100 dark:bg-red-950/20 dark:hover:bg-red-950/40 text-red-650 dark:text-red-400 border border-red-200 dark:border-red-900/30 rounded-lg text-xs font-semibold transition-all disabled:opacity-50 active:scale-98"
          >
            {isRowMutating ? 'Disputing...' : 'Raise Dispute'}
          </button>
        )}

        {canResolve && (
          <button
            onClick={() => onResolve(settlement.id)}
            disabled={isMutating}
            aria-label={`Resolve dispute and mark ${formatAmount(settlement.amount, settlement.currency)} as paid`}
            className="px-3.5 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold shadow-sm hover:shadow-indigo-600/10 transition-all disabled:opacity-50 active:scale-98"
          >
            {isRowMutating ? 'Resolving...' : 'Resolve Dispute'}
          </button>
        )}

        {settlement.status === 'DISPUTED' && !canResolve && (
          <span className="text-xs text-red-500 font-semibold italic select-none" role="status">
            Under review by admin
          </span>
        )}
      </div>
    </div>
  );
};

export const SettlementRow = React.memo(SettlementRowComponent);
