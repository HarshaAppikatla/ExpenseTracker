import React from 'react';
import { SettlementResponse } from '../types';
import { DisputeDialog } from './DisputeDialog';
import { SettlementRow } from './SettlementRow';
import { EmptySettlementState } from './EmptySettlementState';
import { GroupRole } from '../../group/types/group';

interface SettlementListProps {
  settlements: SettlementResponse[];
  currentUserId: string;
  currentUserRole: GroupRole;
  isGroupArchived: boolean;
  onConfirmPayment: (id: string) => void;
  onDispute: (id: string, reason: string) => void;
  onResolve: (id: string) => void;
  isMutating: boolean;
  activeMutatingId: string | null;
  onDisputeClick: (id: string) => void;
  disputeId: string | null;
  onDisputeClose: () => void;
}

export const SettlementList: React.FC<SettlementListProps> = ({
  settlements,
  currentUserId,
  currentUserRole,
  isGroupArchived,
  onConfirmPayment,
  onDispute,
  onResolve,
  isMutating,
  activeMutatingId,
  onDisputeClick,
  disputeId,
  onDisputeClose,
}) => {
  const [filter, setFilter] = React.useState<'all' | 'pending' | 'disputed' | 'confirmed'>('all');

  const formatAmount = (val: number, currency: string) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency || 'INR',
    }).format(val);
  };

  const filteredSettlements = React.useMemo(() => {
    return settlements.filter((s) => {
      if (filter === 'all') return true;
      return s.status.toLowerCase() === filter;
    });
  }, [settlements, filter]);

  const targetDispute = React.useMemo(() => {
    return settlements.find((s) => s.id === disputeId);
  }, [settlements, disputeId]);

  return (
    <div className="space-y-4">
      {/* Internal Filter Tabs */}
      <div className="flex gap-2 border-b border-slate-200 dark:border-slate-800 pb-2">
        {(['all', 'pending', 'disputed', 'confirmed'] as const).map((opt) => {
          const count = opt === 'all' ? settlements.length : settlements.filter((s) => s.status.toLowerCase() === opt).length;
          return (
            <button
              key={opt}
              onClick={() => setFilter(opt)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold capitalize transition-all ${
                filter === opt
                  ? 'bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-white'
                  : 'text-slate-400 hover:text-slate-655 dark:text-slate-500 dark:hover:text-slate-350'
              }`}
            >
              {opt} ({count})
            </button>
          );
        })}
      </div>

      {filteredSettlements.length === 0 ? (
        <EmptySettlementState
          message={`No ${filter !== 'all' ? filter : ''} settlements found`}
          subMessage={
            filter === 'all'
              ? 'Everything is fully settled within the group!'
              : `No settlements currently in the "${filter}" state.`
          }
        />
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {filteredSettlements.map((settlement) => (
            <SettlementRow
              key={settlement.id}
              settlement={settlement}
              currentUserId={currentUserId}
              currentUserRole={currentUserRole}
              isGroupArchived={isGroupArchived}
              onConfirmPayment={onConfirmPayment}
              onDisputeClick={onDisputeClick}
              onResolve={onResolve}
              isMutating={isMutating}
              activeMutatingId={activeMutatingId}
            />
          ))}
        </div>
      )}

      {/* Dispute Modal Dialog */}
      {targetDispute && (
        <DisputeDialog
          isOpen={disputeId !== null}
          onClose={onDisputeClose}
          onConfirm={(reason) => onDispute(targetDispute.id, reason)}
          isLoading={isMutating}
          debtorName={targetDispute.fromUserName}
          amountText={formatAmount(targetDispute.amount, targetDispute.currency)}
        />
      )}
    </div>
  );
};
