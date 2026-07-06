import React from 'react';
import {
  useSettlementSummary,
  useGenerateSettlements,
  useConfirmPayment,
  useDisputeSettlement,
  useResolveSettlement,
} from '../hooks/useSettlements';
import { SettlementSummary } from './SettlementSummary';
import { SettlementList } from './SettlementList';
import { LoadingSettlementSkeleton } from './LoadingSettlementSkeleton';
import { ConfirmationDialog } from './ConfirmationDialog';
import { settlementPermissions } from '../utils/permissions';
import { GroupRole } from '../../group/types/group';
import { useQueryClient } from '@tanstack/react-query';
import { settlementKeys } from '../hooks/settlementKeys';

interface SettlementDashboardProps {
  groupId: string;
  currentUserId: string;
  currentUserRole: GroupRole;
  isGroupArchived: boolean;
}

export const SettlementDashboard: React.FC<SettlementDashboardProps> = ({
  groupId,
  currentUserId,
  currentUserRole,
  isGroupArchived,
}) => {
  const queryClient = useQueryClient();
  
  const [activeMutatingId, setActiveMutatingId] = React.useState<string | null>(null);
  const [disputeId, setDisputeId] = React.useState<string | null>(null);
  
  // Dialog States
  const [showGenerateConfirm, setShowGenerateConfirm] = React.useState(false);
  const [confirmPaymentId, setConfirmPaymentId] = React.useState<string | null>(null);
  const [resolveDisputeId, setResolveDisputeId] = React.useState<string | null>(null);

  // Queries
  const { data: summary, isLoading, isError, error, refetch } = useSettlementSummary(groupId);

  // Mutations
  const generateMutation = useGenerateSettlements(groupId);
  const confirmPaymentMutation = useConfirmPayment(groupId);
  const disputeMutation = useDisputeSettlement(groupId);
  const resolveMutation = useResolveSettlement(groupId);

  const canGenerate = settlementPermissions.canGenerate(currentUserRole, isGroupArchived);

  const isMutating =
    generateMutation.isPending ||
    confirmPaymentMutation.isPending ||
    disputeMutation.isPending ||
    resolveMutation.isPending;

  // Reset mutating ID once operations complete
  React.useEffect(() => {
    if (!isMutating) {
      setActiveMutatingId(null);
    }
  }, [isMutating]);

  if (isLoading) {
    return <LoadingSettlementSkeleton />;
  }

  if (isError || !summary) {
    return (
      <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 text-red-800 dark:text-red-300 p-6 rounded-2xl text-center space-y-4 shadow-sm">
        <div className="flex flex-col items-center gap-2">
          <svg className="w-8 h-8 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <p className="font-bold">Failed to load settlements</p>
          <p className="text-xs text-slate-500 dark:text-slate-400 max-w-md mx-auto">
            {error?.message || 'Access denied or calculations failed.'}
          </p>
        </div>
        <div className="flex gap-2 justify-center">
          <button
            onClick={() => refetch()}
            className="px-4 py-1.5 bg-slate-900 dark:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-sm hover:bg-slate-800 dark:hover:bg-slate-700 transition-colors"
          >
            Retry Loading
          </button>
          {canGenerate && (
            <button
              onClick={() => setShowGenerateConfirm(true)}
              className="px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold shadow-sm transition-colors"
            >
              Generate Settlements
            </button>
          )}
        </div>
      </div>
    );
  }

  const handleGenerateConfirm = () => {
    // Invalidate everything on regenerate
    queryClient.invalidateQueries({ queryKey: settlementKeys.all });
    generateMutation.mutate(undefined);
  };

  const handleConfirmPayment = () => {
    if (confirmPaymentId) {
      setActiveMutatingId(confirmPaymentId);
      confirmPaymentMutation.mutate({ id: confirmPaymentId, request: {} });
      setConfirmPaymentId(null);
    }
  };

  const handleDispute = (settlementId: string, reason: string) => {
    setActiveMutatingId(settlementId);
    disputeMutation.mutate(
      { id: settlementId, request: { reason } },
      {
        onSuccess: () => {
          setDisputeId(null);
        },
      }
    );
  };

  const handleResolve = () => {
    if (resolveDisputeId) {
      setActiveMutatingId(resolveDisputeId);
      resolveMutation.mutate(resolveDisputeId);
      setResolveDisputeId(null);
    }
  };

  return (
    <div className="space-y-6">
      <SettlementSummary
        summary={summary}
        isAdminOrOwner={canGenerate}
        onGenerateClick={() => setShowGenerateConfirm(true)}
        isGenerating={generateMutation.isPending}
      />

      <SettlementList
        settlements={summary.settlements || []}
        currentUserId={currentUserId}
        currentUserRole={currentUserRole}
        isGroupArchived={isGroupArchived}
        onConfirmPayment={(id) => setConfirmPaymentId(id)}
        onDispute={handleDispute}
        onResolve={(id) => setResolveDisputeId(id)}
        isMutating={isMutating}
        activeMutatingId={activeMutatingId}
        onDisputeClick={(id) => setDisputeId(id)}
        disputeId={disputeId}
        onDisputeClose={() => setDisputeId(null)}
      />

      {/* Recalculate Confirmation Dialog */}
      <ConfirmationDialog
        isOpen={showGenerateConfirm}
        onClose={() => setShowGenerateConfirm(false)}
        onConfirm={handleGenerateConfirm}
        title="Recalculate Group Settlements?"
        description="This will clear and recalculate all pending outstanding settlements using our debt simplification engine. Existing disputed settlements will remain frozen and unaffected."
        confirmLabel="Recalculate"
        confirmVariant="primary"
      />

      {/* Confirm Payment Dialog */}
      <ConfirmationDialog
        isOpen={confirmPaymentId !== null}
        onClose={() => setConfirmPaymentId(null)}
        onConfirm={handleConfirmPayment}
        title="Confirm Payment?"
        description="Are you sure you want to mark this transaction as paid? This will transition its status to settled."
        confirmLabel="Confirm Paid"
        confirmVariant="success"
      />

      {/* Resolve Dispute Dialog */}
      <ConfirmationDialog
        isOpen={resolveDisputeId !== null}
        onClose={() => setResolveDisputeId(null)}
        onConfirm={handleResolve}
        title="Resolve Active Dispute?"
        description="Are you sure you want to resolve this active dispute? This will confirm the payment and mark it as settled."
        confirmLabel="Resolve & Confirm"
        confirmVariant="primary"
      />
    </div>
  );
};
