import React, { useState } from 'react';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useGroupExpenses } from '../hooks/useExpenses';
import {
  useCreateGroupExpense,
  useUpdateGroupExpense,
  useDeleteGroupExpense,
  useTransitionGroupExpenseStatus,
  useAddGroupExpenseAttachment,
} from '../hooks/useExpenseMutations';
import { GroupExpense, GroupExpenseCategory, GroupExpenseStatus } from '../services/expenseService';
import { CreateExpenseDialog } from './CreateExpenseDialog';
import { GroupMemberDto } from '@/features/group/types/group';
import {
  Plus,
  Trash2,
  Edit2,
  Check,
  Ban,
  Paperclip,
  ChevronDown,
  ChevronUp,
  FileText,
  Calendar,
  User,
  Users,
} from 'lucide-react';

interface ExpenseListProps {
  groupId: string;
  tripId?: string | null;
  members: GroupMemberDto[];
}

export const ExpenseList: React.FC<ExpenseListProps> = ({ groupId, tripId, members }) => {
  const [page, setPage] = useState(0);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingExpense, setEditingExpense] = useState<GroupExpense | null>(null);
  const [expandedExpenseId, setExpandedExpenseId] = useState<string | null>(null);

  const { user } = useAuthContext();
  const currentUserId = user?.id || '';

  // Determine current user's role in this group
  const currentMember = members.find((m) => m.userId === currentUserId);
  const isGroupAdmin = currentMember?.role === 'OWNER' || currentMember?.role === 'ADMIN';

  // Queries
  const { data, isLoading, isError, error } = useGroupExpenses(groupId, tripId, page, 10);

  // Mutations
  const createMutation = useCreateGroupExpense(groupId);
  const updateMutation = useUpdateGroupExpense(groupId);
  const deleteMutation = useDeleteGroupExpense(groupId);
  const statusMutation = useTransitionGroupExpenseStatus(groupId);
  const addAttachmentMutation = useAddGroupExpenseAttachment(groupId);

  const handleCreateSubmit = (payload: any) => {
    createMutation.mutate(payload, {
      onSuccess: () => {
        setIsCreateOpen(false);
      },
    });
  };

  const handleUpdateSubmit = (payload: any) => {
    if (editingExpense) {
      updateMutation.mutate(
        { id: editingExpense.id, data: payload },
        {
          onSuccess: () => {
            setEditingExpense(null);
          },
        }
      );
    }
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Are you sure you want to delete this expense?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleStatusTransition = (id: string, currentStatus: GroupExpenseStatus) => {
    const nextStatus: GroupExpenseStatus = currentStatus === 'DRAFT' ? 'POSTED' : 'VOID';
    const actionLabel = currentStatus === 'DRAFT' ? 'POST/LOCK' : 'VOID/ARCHIVE';
    
    if (window.confirm(`Are you sure you want to ${actionLabel} this expense?`)) {
      statusMutation.mutate({ id, status: nextStatus });
    }
  };

  const handleAddMockAttachment = (expenseId: string) => {
    const mockFiles = [
      { name: 'uber_receipt.pdf', size: 102400, type: 'application/pdf', url: 'https://storage.local/receipts/uber_receipt_123.pdf' },
      { name: 'hotel_invoice.png', size: 2048500, type: 'image/png', url: 'https://storage.local/receipts/hotel_invoice_456.png' },
      { name: 'dinner_bill.jpg', size: 512000, type: 'image/jpeg', url: 'https://storage.local/receipts/dinner_bill_789.jpg' },
    ];
    const chosenFile = mockFiles[Math.floor(Math.random() * mockFiles.length)];

    addAttachmentMutation.mutate({
      expenseId,
      url: chosenFile.url,
      fileName: chosenFile.name,
      fileSize: chosenFile.size,
      fileType: chosenFile.type,
      storageProvider: 'LOCAL',
    });
  };

  const toggleExpand = (id: string) => {
    setExpandedExpenseId((prev) => (prev === id ? null : id));
  };

  const getCategoryColor = (cat: GroupExpenseCategory) => {
    switch (cat) {
      case 'FOOD': return 'bg-amber-100 text-amber-800 dark:bg-amber-950/30 dark:text-amber-300 border-amber-200 dark:border-amber-900/30';
      case 'LODGING': return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-950/30 dark:text-indigo-300 border-indigo-200 dark:border-indigo-900/30';
      case 'TRANSPORT': return 'bg-sky-100 text-sky-850 dark:bg-sky-950/30 dark:text-sky-300 border-sky-200 dark:border-sky-900/30';
      case 'ENTERTAINMENT': return 'bg-purple-100 text-purple-800 dark:bg-purple-950/30 dark:text-purple-300 border-purple-200 dark:border-purple-900/30';
      case 'SHOPPING': return 'bg-rose-100 text-rose-800 dark:bg-rose-950/30 dark:text-rose-300 border-rose-200 dark:border-rose-900/30';
      default: return 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-300 border-slate-200 dark:border-slate-700/30';
    }
  };

  const getStatusBadge = (status: GroupExpenseStatus) => {
    switch (status) {
      case 'DRAFT': return 'bg-slate-50 text-slate-600 border-slate-200 dark:bg-slate-850 dark:text-slate-400 dark:border-slate-750';
      case 'POSTED': return 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/20 dark:text-emerald-450 dark:border-emerald-900/40';
      case 'VOID': return 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/20 dark:text-red-300 dark:border-red-900/40';
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-4 animate-pulse">
        {[1, 2, 3].map((n) => (
          <div key={n} className="h-28 bg-slate-100 dark:bg-slate-800 rounded-xl" />
        ))}
      </div>
    );
  }

  if (isError) {
    return (
      <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 text-red-800 dark:text-red-300 p-4 rounded-xl text-sm">
        Failed to load expenses: {error?.message}
      </div>
    );
  }

  const expenses = data?.content || [];

  return (
    <div className="space-y-6">
      {/* Header toolbar */}
      <div className="flex items-center justify-between gap-4">
        <div>
          <h3 className="text-lg font-black text-slate-900 dark:text-white">Shared Ledger</h3>
          <p className="text-xs text-slate-400 dark:text-slate-500">
            Record payments and split costs among group members
          </p>
        </div>

        <button
          onClick={() => setIsCreateOpen(true)}
          className="flex items-center gap-1.5 px-4 py-2 bg-slate-900 dark:bg-white text-white dark:text-slate-900 rounded-xl text-xs font-bold hover:bg-slate-800 dark:hover:bg-slate-100 transition-all shadow-sm"
        >
          <Plus className="w-4 h-4" />
          Add Expense
        </button>
      </div>

      {/* Expenses list */}
      {expenses.length === 0 ? (
        <div className="bg-slate-50 dark:bg-slate-950/20 border border-slate-150 dark:border-slate-850 rounded-xl p-10 text-center text-slate-400">
          <FileText className="w-10 h-10 mx-auto mb-3 opacity-60" />
          <p className="font-semibold text-sm">No expenses recorded yet</p>
          <p className="text-xs opacity-80 mt-1">Be the first to add an expense to this group ledger!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {expenses.map((e) => {
            const isExpanded = expandedExpenseId === e.id;
            return (
              <div
                key={e.id}
                className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow"
              >
                {/* Header item summary */}
                <div className="p-4 flex items-center justify-between gap-4 flex-wrap sm:flex-nowrap">
                  <div className="space-y-1.5 flex-1 min-w-[200px]">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getCategoryColor(e.category)}`}>
                        {e.category}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getStatusBadge(e.status)}`}>
                        {e.status}
                      </span>
                      {e.tripId && (
                        <span className="bg-slate-50 dark:bg-slate-950 text-slate-500 border border-slate-200 dark:border-slate-800 px-2 py-0.5 rounded-full text-[10px] font-bold">
                          Trip associated
                        </span>
                      )}
                    </div>
                    <h4 className="font-extrabold text-sm text-slate-850 dark:text-white leading-tight">
                      {e.description}
                    </h4>
                    <div className="flex items-center gap-4 text-[11px] text-slate-400 dark:text-slate-500 font-medium">
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3.5 h-3.5" />
                        {e.expenseDate}
                      </span>
                      <span className="flex items-center gap-1">
                        <User className="w-3.5 h-3.5" />
                        Paid by: <span className="font-bold text-slate-700 dark:text-slate-350">{e.paidByUserName}</span>
                      </span>
                    </div>
                  </div>

                  {/* Pricing / Right panel */}
                  <div className="flex items-center gap-4 shrink-0 justify-between w-full sm:w-auto">
                    <div className="text-right">
                      <div className="text-base font-black text-slate-900 dark:text-white">
                        {e.amount.toFixed(2)} <span className="text-[10px] text-slate-400">{e.currency}</span>
                      </div>
                      <button
                        onClick={() => toggleExpand(e.id)}
                        className="text-[10px] font-bold text-slate-400 hover:text-slate-650 flex items-center gap-0.5 ml-auto"
                      >
                        {isExpanded ? (
                          <>
                            Hide splits <ChevronUp className="w-3 h-3" />
                          </>
                        ) : (
                          <>
                            Show splits <ChevronDown className="w-3 h-3" />
                          </>
                        )}
                      </button>
                    </div>

                    {/* Action buttons — only visible to the payer or group admins/owners */}
                    {(e.paidByUserId === currentUserId || isGroupAdmin) && (
                      <div className="flex items-center gap-2">
                        {e.status === 'DRAFT' ? (
                          <>
                            <button
                              onClick={() => handleStatusTransition(e.id, 'DRAFT')}
                              title="Post/Lock Ledger"
                              className="p-2.5 bg-emerald-50 hover:bg-emerald-100 text-emerald-600 rounded-xl transition-colors border border-emerald-200 shadow-sm"
                            >
                              <Check className="w-5 h-5" />
                            </button>
                            <button
                              onClick={() => setEditingExpense(e)}
                              title="Edit"
                              className="p-2.5 bg-slate-50 hover:bg-slate-100 text-slate-600 dark:bg-slate-800 dark:hover:bg-slate-750 dark:text-slate-300 rounded-xl transition-colors border border-slate-200 dark:border-slate-700 shadow-sm"
                            >
                              <Edit2 className="w-5 h-5" />
                            </button>
                          </>
                        ) : e.status === 'POSTED' ? (
                          <>
                            <button
                              onClick={() => handleStatusTransition(e.id, 'POSTED')}
                              title="Void Ledger"
                              className="p-2.5 bg-red-50 hover:bg-red-100 text-red-600 rounded-xl transition-colors border border-red-200 shadow-sm"
                            >
                              <Ban className="w-5 h-5" />
                            </button>
                            <button
                              onClick={() => handleAddMockAttachment(e.id)}
                              title="Attach Receipt"
                              className="p-2.5 bg-slate-50 hover:bg-slate-100 text-slate-600 dark:bg-slate-800 dark:hover:bg-slate-750 dark:text-slate-300 rounded-xl transition-colors border border-slate-200 dark:border-slate-700 shadow-sm"
                            >
                              <Paperclip className="w-5 h-5" />
                            </button>
                          </>
                        ) : null}

                        {/* Delete is allowed for payer/admin in all states */}
                        <button
                          onClick={() => handleDelete(e.id)}
                          title="Delete"
                          className="p-2.5 bg-red-50 hover:bg-red-100 text-red-600 rounded-xl transition-colors border border-red-200 shadow-sm"
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                    )}
                  </div>
                </div>

                {/* Expanded splits block */}
                {isExpanded && (
                  <div className="bg-slate-50 dark:bg-slate-950/30 border-t border-slate-100 dark:border-slate-800/80 px-6 py-4 space-y-3">
                    <h5 className="text-[10px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 flex items-center gap-1">
                      <Users className="w-3.5 h-3.5" /> Debt Distributions
                    </h5>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-2">
                      {e.splits.map((s) => (
                        <div key={s.id} className="flex justify-between items-center py-1 text-xs border-b border-slate-100 dark:border-slate-850/30">
                          <span className="text-slate-600 dark:text-slate-350">
                            {s.userName} {s.userId === e.paidByUserId && <span className="text-[9px] bg-slate-200 dark:bg-slate-850 px-1 py-0.5 rounded font-bold ml-1 text-slate-500">Payer</span>}
                          </span>
                          <span className="font-bold text-slate-800 dark:text-slate-200">
                            {s.owedAmount.toFixed(2)} <span className="text-[9px] text-slate-400">{e.currency}</span>
                          </span>
                        </div>
                      ))}
                    </div>

                    {/* Attachments preview */}
                    {e.attachments && e.attachments.length > 0 && (
                      <div className="pt-2 border-t border-slate-100 dark:border-slate-800/40">
                        <h5 className="text-[10px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500 mb-2">
                          Attachments ({e.attachments.length})
                        </h5>
                        <div className="flex gap-2 flex-wrap">
                          {e.attachments.map((att) => (
                            <a
                              key={att.id}
                              href={att.url}
                              target="_blank"
                              rel="noreferrer"
                              className="inline-flex items-center gap-1.5 px-3 py-1 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-lg text-xs font-bold text-slate-650 hover:bg-slate-50 dark:text-slate-350 transition-colors"
                            >
                              <Paperclip className="w-3.5 h-3.5 text-slate-400" />
                              {att.fileName}
                            </a>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Pagination controls */}
      {data && data.totalPages > 1 && (
        <div className="flex justify-center gap-2">
          {Array.from({ length: data.totalPages }).map((_, idx) => (
            <button
              key={idx}
              onClick={() => setPage(idx)}
              className={`px-3 py-1 text-xs font-bold rounded-lg border transition-all ${
                page === idx
                  ? 'bg-slate-900 border-slate-900 text-white dark:bg-white dark:border-white dark:text-slate-900'
                  : 'bg-white border-slate-200 text-slate-650 hover:bg-slate-50 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-750'
              }`}
            >
              {idx + 1}
            </button>
          ))}
        </div>
      )}

      {/* Create dialog modal */}
      <CreateExpenseDialog
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleCreateSubmit}
        isLoading={createMutation.isPending}
        members={members}
        tripId={tripId}
      />

      {/* Edit dialog modal */}
      <CreateExpenseDialog
        isOpen={editingExpense !== null}
        onClose={() => setEditingExpense(null)}
        onSubmit={handleUpdateSubmit}
        isLoading={updateMutation.isPending}
        members={members}
        expense={editingExpense}
        tripId={tripId}
      />
    </div>
  );
};
