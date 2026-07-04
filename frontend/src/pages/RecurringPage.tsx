import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  useRecurringTransactions, useCreateRecurring, useUpdateRecurring,
  useDeleteRecurring, usePauseRecurring, useResumeRecurring
} from '@/features/recurring/hooks/useRecurring';
import { useCategories } from '@/features/category/hooks/useCategories';
import { RecurringTransaction, RecurringRequest } from '@/features/recurring/services/recurringService';
import {
  PlusCircle, Trash2, Edit2, X, Repeat2,
  Pause, Play, TrendingDown, TrendingUp, Clock
} from 'lucide-react';

const fmtDate = (d: string) => new Date(d).toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' });

const STATUS_BADGE: Record<string, string> = {
  ACTIVE:    'bg-emerald-100 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-400',
  PAUSED:    'bg-amber-100 dark:bg-amber-950/40 text-amber-700 dark:text-amber-400',
  COMPLETED: 'bg-blue-100 dark:bg-blue-950/40 text-blue-700 dark:text-blue-400',
  EXPIRED:   'bg-slate-100 dark:bg-slate-800 text-slate-500',
};

interface FormState {
  transactionType: 'EXPENSE' | 'INCOME';
  categoryId: string;
  amount: string;
  currencyCode: string;
  merchant: string;
  description: string;
  recurrenceType: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
  recurrenceInterval: string;
  startDate: string;
  endDate: string;
}

const emptyForm: FormState = {
  transactionType: 'EXPENSE', categoryId: '', amount: '',
  currencyCode: 'USD', merchant: '', description: '',
  recurrenceType: 'MONTHLY', recurrenceInterval: '1',
  startDate: new Date().toISOString().substring(0, 10), endDate: '',
};

export const RecurringPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { data: recurrings = [], isLoading } = useRecurringTransactions();
  const { data: categories = [] } = useCategories();
  const create = useCreateRecurring();
  const update = useUpdateRecurring();
  const remove = useDeleteRecurring();
  const pause = usePauseRecurring();
  const resume = useResumeRecurring();

  const [showForm, setShowForm] = useState(location.pathname.endsWith('/new'));
  const [editItem, setEditItem] = useState<RecurringTransaction | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);

  const closeForm = () => {
    setShowForm(false);
    if (location.pathname.endsWith('/new')) {
      navigate('/recurring');
    }
  };

  const openCreate = () => { setEditItem(null); setForm(emptyForm); setShowForm(true); };
  const openEdit = (r: RecurringTransaction) => {
    setEditItem(r);
    setForm({
      transactionType: r.transactionType,
      categoryId: r.categoryId || '',
      amount: String(r.amount),
      currencyCode: r.currencyCode,
      merchant: r.merchant || '',
      description: r.description || '',
      recurrenceType: r.recurrenceType,
      recurrenceInterval: String(r.recurrenceInterval),
      startDate: r.startDate.substring(0, 10),
      endDate: r.endDate ? r.endDate.substring(0, 10) : '',
    });
    setShowForm(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const payload: RecurringRequest = {
      transactionType: form.transactionType,
      categoryId: form.categoryId || undefined,
      amount: parseFloat(form.amount),
      currencyCode: form.currencyCode,
      merchant: form.merchant || undefined,
      description: form.description || undefined,
      recurrenceType: form.recurrenceType,
      recurrenceInterval: parseInt(form.recurrenceInterval),
      startDate: form.startDate + 'T00:00:00',
      endDate: form.endDate ? form.endDate + 'T00:00:00' : undefined,
    };
    if (editItem) {
      update.mutate({ id: editItem.id, data: payload }, { onSuccess: () => { closeForm(); setEditItem(null); } });
    } else {
      create.mutate(payload, { onSuccess: () => closeForm() });
    }
  };

  const active = recurrings.filter((r) => r.status === 'ACTIVE').length;

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Recurring Transactions</h1>
          <p className="text-xs text-slate-400 mt-0.5">Automate subscriptions, rent, salary, and other regular payments</p>
        </div>
        <button
          onClick={openCreate}
          className="inline-flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-xl text-sm font-semibold hover:bg-primary/90 active:scale-95 transition-all shadow-md shadow-primary/20"
        >
          <PlusCircle size={16} /> New Recurring
        </button>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Total Templates', value: recurrings.length, color: 'indigo' },
          { label: 'Active', value: active, color: 'emerald' },
          { label: 'Paused / Expired', value: recurrings.length - active, color: 'slate' },
        ].map((s) => (
          <div key={s.label} className="bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-2xl p-4 text-center">
            <p className={`text-2xl font-extrabold text-${s.color}-600 dark:text-${s.color}-400`}>{s.value}</p>
            <p className="text-[11px] text-slate-400 mt-0.5">{s.label}</p>
          </div>
        ))}
      </div>

      {/* List */}
      {isLoading ? (
        <div className="text-center py-16 text-slate-400">Loading recurring templates...</div>
      ) : recurrings.length === 0 ? (
        <div className="flex flex-col items-center gap-3 py-20">
          <div className="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
            <Repeat2 size={28} className="text-slate-400" />
          </div>
          <p className="text-slate-600 dark:text-slate-300 font-semibold">No recurring templates yet</p>
          <p className="text-sm text-slate-400">Add subscriptions or regular payments to automate your ledger.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {recurrings.map((r) => (
            <div key={r.id} className="bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-2xl p-4 hover:shadow-sm transition-shadow">
              <div className="flex items-center justify-between gap-3">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${r.transactionType === 'EXPENSE' ? 'bg-red-50 dark:bg-red-950/20' : 'bg-emerald-50 dark:bg-emerald-950/20'}`}>
                    {r.transactionType === 'EXPENSE' ? (
                      <TrendingDown size={16} className="text-red-500" />
                    ) : (
                      <TrendingUp size={16} className="text-emerald-500" />
                    )}
                  </div>
                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap cursor-pointer hover:underline" onClick={() => navigate(`/recurring/${r.id}`)}>
                      <span className="font-semibold text-slate-800 dark:text-slate-100 truncate">
                        {r.merchant || r.description || r.categoryName || 'Unnamed Template'}
                      </span>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${STATUS_BADGE[r.status]}`}>
                        {r.status}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 mt-0.5 text-xs text-slate-400 flex-wrap">
                      <span>{r.recurrenceInterval}× {r.recurrenceType.toLowerCase()}</span>
                      {r.categoryName && <span>· {r.categoryName}</span>}
                      <span className="flex items-center gap-1">
                        <Clock size={10} /> Next: {fmtDate(r.nextExecution)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <span className={`font-bold text-sm ${r.transactionType === 'EXPENSE' ? 'text-red-600 dark:text-red-400' : 'text-emerald-600 dark:text-emerald-400'}`}>
                    {r.transactionType === 'EXPENSE' ? '−' : '+'}${r.amount.toFixed(2)}
                  </span>
                  <div className="flex gap-1">
                    {r.status === 'ACTIVE' ? (
                      <button onClick={() => pause.mutate(r.id)} title="Pause" className="p-2 rounded-xl hover:bg-amber-50 dark:hover:bg-amber-950/20 text-slate-400 hover:text-amber-600 transition-colors">
                        <Pause size={14} />
                      </button>
                    ) : r.status === 'PAUSED' ? (
                      <button onClick={() => resume.mutate(r.id)} title="Resume" className="p-2 rounded-xl hover:bg-emerald-50 dark:hover:bg-emerald-950/20 text-slate-400 hover:text-emerald-600 transition-colors">
                        <Play size={14} />
                      </button>
                    ) : null}
                    <button onClick={() => openEdit(r)} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 hover:text-slate-600 transition-colors">
                      <Edit2 size={14} />
                    </button>
                    <button onClick={() => remove.mutate(r.id)} className="p-2 rounded-xl hover:bg-red-50 dark:hover:bg-red-950/20 text-slate-400 hover:text-red-500 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Form Modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
          <div className="bg-white dark:bg-dark-surface rounded-2xl shadow-2xl w-full max-w-md p-6 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-5">
              <h2 className="font-bold text-lg text-slate-800 dark:text-slate-100">
                {editItem ? 'Edit Recurring' : 'New Recurring Template'}
              </h2>
              <button onClick={closeForm} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400">
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Type</label>
                  <select value={form.transactionType} onChange={(e) => setForm({ ...form, transactionType: e.target.value as any })}
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50">
                    <option value="EXPENSE">Expense</option>
                    <option value="INCOME">Income</option>
                  </select>
                </div>
                <div>
                  <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Amount ($)</label>
                  <input type="number" min="0.01" step="0.01" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })}
                    placeholder="0.00" required
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50" />
                </div>
              </div>

              {form.transactionType === 'EXPENSE' && (
                <div>
                  <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Category (optional)</label>
                  <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50">
                    <option value="">No category</option>
                    {categories.map((c: any) => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
              )}

              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Merchant / Source</label>
                <input value={form.merchant} onChange={(e) => setForm({ ...form, merchant: e.target.value })}
                  placeholder="e.g. Netflix, Rent, Salary"
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50" />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Frequency</label>
                  <select value={form.recurrenceType} onChange={(e) => setForm({ ...form, recurrenceType: e.target.value as any })}
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50">
                    <option value="DAILY">Daily</option>
                    <option value="WEEKLY">Weekly</option>
                    <option value="MONTHLY">Monthly</option>
                    <option value="YEARLY">Yearly</option>
                  </select>
                </div>
                <div>
                  <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Every N times</label>
                  <input type="number" min="1" value={form.recurrenceInterval} onChange={(e) => setForm({ ...form, recurrenceInterval: e.target.value })}
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50" />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Start Date</label>
                  <input type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} required
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50" />
                </div>
                <div>
                  <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">End Date (opt.)</label>
                  <input type="date" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50" />
                </div>
              </div>

              <div className="flex gap-3 pt-2">
                <button type="button" onClick={closeForm} className="flex-1 py-2.5 rounded-xl border border-light-border dark:border-dark-border text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                  Cancel
                </button>
                <button type="submit" disabled={create.isPending || update.isPending} className="flex-1 py-2.5 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 disabled:opacity-60 transition-colors">
                  {editItem ? 'Save Changes' : 'Create Template'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </motion.div>
  );
};
