import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { useNavigate, useLocation } from 'react-router-dom';
import { useBudgets, useCreateBudget, useUpdateBudget, useDeleteBudget } from '@/features/budget/hooks/useBudget';
import { useCategories } from '@/features/category/hooks/useCategories';
import { BudgetRequest } from '@/features/budget/services/budgetService';
import {
  PlusCircle, Trash2, Edit2, AlertTriangle, CheckCircle2, TrendingUp,
  X, ChevronLeft, ChevronRight, Target, Wallet, BarChart2
} from 'lucide-react';

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

const clamp = (n: number, min: number, max: number) => Math.min(Math.max(n, min), max);

const utilizationColor = (pct: number) => {
  if (pct >= 100) return { bar: 'bg-red-500', text: 'text-red-600 dark:text-red-400', bg: 'bg-red-50 dark:bg-red-950/20 border-red-200 dark:border-red-900/40' };
  if (pct >= 80) return { bar: 'bg-amber-500', text: 'text-amber-600 dark:text-amber-400', bg: 'bg-amber-50 dark:bg-amber-950/20 border-amber-200 dark:border-amber-900/40' };
  return { bar: 'bg-emerald-500', text: 'text-emerald-600 dark:text-emerald-400', bg: 'bg-emerald-50 dark:bg-emerald-950/20 border-emerald-200 dark:border-emerald-900/40' };
};

interface FormState { categoryId: string; monthlyLimit: string; alertPercentage: string; currencyCode: string; }
const emptyForm: FormState = { categoryId: '', monthlyLimit: '', alertPercentage: '80', currencyCode: 'USD' };

export const BudgetPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [showForm, setShowForm] = useState(location.pathname.endsWith('/new'));
  const [editId, setEditId] = useState<string | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);

  const { data: budgets = [], isLoading } = useBudgets(year, month);
  const { data: categories = [] } = useCategories();
  const createBudget = useCreateBudget();
  const updateBudget = useUpdateBudget();
  const deleteBudget = useDeleteBudget();

  const closeForm = () => {
    setShowForm(false);
    if (location.pathname.endsWith('/new')) {
      navigate('/budgets');
    }
  };

  const navMonth = (dir: 1 | -1) => {
    let m = month + dir;
    let y = year;
    if (m > 12) { m = 1; y++; }
    if (m < 1)  { m = 12; y--; }
    setMonth(m); setYear(y);
  };

  const openCreate = () => { setEditId(null); setForm(emptyForm); setShowForm(true); };
  const openEdit = (bp: typeof budgets[0]) => {
    setEditId(bp.budget.id);
    setForm({
      categoryId: bp.budget.categoryId,
      monthlyLimit: String(bp.budget.monthlyLimit),
      alertPercentage: String(bp.budget.alertPercentage),
      currencyCode: bp.budget.currencyCode,
    });
    setShowForm(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const payload: BudgetRequest = {
      categoryId: form.categoryId,
      year,
      month,
      monthlyLimit: parseFloat(form.monthlyLimit),
      currencyCode: form.currencyCode,
      alertPercentage: parseInt(form.alertPercentage),
    };
    if (editId) {
      updateBudget.mutate({ id: editId, data: payload }, { onSuccess: () => { closeForm(); setEditId(null); } });
    } else {
      createBudget.mutate(payload, { onSuccess: () => closeForm() });
    }
  };

  const totalLimit = budgets.reduce((s, b) => s + b.budget.monthlyLimit, 0);
  const totalSpent = budgets.reduce((s, b) => s + b.currentSpent, 0);
  const overallPct = totalLimit > 0 ? clamp((totalSpent / totalLimit) * 100, 0, 100) : 0;

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Budget Planner</h1>
          <p className="text-xs text-slate-400 mt-0.5">Set monthly spending limits per category and track utilization</p>
        </div>
        <button
          onClick={openCreate}
          className="inline-flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-xl text-sm font-semibold hover:bg-primary/90 active:scale-95 transition-all shadow-md shadow-primary/20"
        >
          <PlusCircle size={16} /> Add Budget
        </button>
      </div>

      {/* Month Navigation */}
      <div className="flex items-center justify-between bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-2xl px-5 py-3">
        <button onClick={() => navMonth(-1)} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
          <ChevronLeft size={18} className="text-slate-500" />
        </button>
        <span className="font-bold text-slate-800 dark:text-slate-100 text-base">
          {MONTHS[month - 1]} {year}
        </span>
        <button onClick={() => navMonth(1)} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
          <ChevronRight size={18} className="text-slate-500" />
        </button>
      </div>

      {/* Summary KPIs */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[
          { icon: Wallet, label: 'Total Budget', value: `$${totalLimit.toFixed(2)}`, sub: 'across all categories', color: 'indigo' },
          { icon: TrendingUp, label: 'Total Spent', value: `$${totalSpent.toFixed(2)}`, sub: 'this month so far', color: totalSpent > totalLimit ? 'red' : 'emerald' },
          { icon: BarChart2, label: 'Overall Usage', value: `${overallPct.toFixed(1)}%`, sub: overallPct >= 100 ? 'Budget exceeded!' : 'of budget used', color: overallPct >= 100 ? 'red' : overallPct >= 80 ? 'amber' : 'emerald' },
        ].map((kpi) => (
          <div key={kpi.label} className="bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-2xl p-5">
            <div className="flex justify-between items-start">
              <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">{kpi.label}</span>
              <div className={`p-2 rounded-xl bg-${kpi.color}-50 dark:bg-${kpi.color}-950/20 text-${kpi.color}-500`}>
                <kpi.icon size={16} />
              </div>
            </div>
            <p className="text-2xl font-extrabold text-slate-900 dark:text-slate-50 mt-2">{kpi.value}</p>
            <p className="text-[11px] text-slate-400 mt-0.5">{kpi.sub}</p>
          </div>
        ))}
      </div>

      {/* Budget Cards */}
      {isLoading ? (
        <div className="text-center py-16 text-slate-400">Loading budgets...</div>
      ) : budgets.length === 0 ? (
        <div className="flex flex-col items-center gap-3 py-20">
          <div className="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
            <Target size={28} className="text-slate-400" />
          </div>
          <p className="text-slate-600 dark:text-slate-300 font-semibold">No budgets for {MONTHS[month-1]} {year}</p>
          <p className="text-sm text-slate-400">Click "Add Budget" to set spending limits per category.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {budgets.map((bp) => {
            const pct = clamp(bp.utilizationPercentage, 0, 100);
            const colors = utilizationColor(bp.utilizationPercentage);
            return (
              <div key={bp.budget.id} className={`bg-white dark:bg-dark-surface border rounded-2xl p-5 hover:shadow-md transition-shadow ${colors.bg}`}>
                <div className="flex justify-between items-start mb-3">
                  <div className="cursor-pointer hover:underline" onClick={() => navigate(`/budgets/${bp.budget.id}`)}>
                    <span className="font-bold text-slate-800 dark:text-slate-100">{bp.budget.categoryName}</span>
                    <div className="flex items-center gap-1.5 mt-0.5">
                      {bp.utilizationPercentage >= 100 ? (
                        <span className="flex items-center gap-1 text-[11px] text-red-600 dark:text-red-400 font-semibold">
                          <AlertTriangle size={11} /> Exceeded
                        </span>
                      ) : bp.utilizationPercentage >= bp.budget.alertPercentage ? (
                        <span className="flex items-center gap-1 text-[11px] text-amber-600 dark:text-amber-400 font-semibold">
                          <AlertTriangle size={11} /> Warning ({bp.budget.alertPercentage}%)
                        </span>
                      ) : (
                        <span className="flex items-center gap-1 text-[11px] text-emerald-600 dark:text-emerald-400 font-semibold">
                          <CheckCircle2 size={11} /> On track
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-1">
                    <button onClick={() => openEdit(bp)} className="p-2 rounded-xl hover:bg-white/60 dark:hover:bg-slate-700 text-slate-400 hover:text-slate-600 transition-colors">
                      <Edit2 size={14} />
                    </button>
                    <button onClick={() => deleteBudget.mutate(bp.budget.id)} className="p-2 rounded-xl hover:bg-red-50 dark:hover:bg-red-950/20 text-slate-400 hover:text-red-500 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>

                {/* Progress Bar */}
                <div className="mb-3">
                  <div className="flex justify-between text-xs mb-1.5">
                    <span className="text-slate-500 dark:text-slate-400">
                      ${bp.currentSpent.toFixed(2)} <span className="text-slate-400">of</span> ${bp.budget.monthlyLimit.toFixed(2)}
                    </span>
                    <span className={`font-bold ${colors.text}`}>{bp.utilizationPercentage.toFixed(1)}%</span>
                  </div>
                  <div className="h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-700 ${colors.bar}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>

                <div className="flex justify-between text-xs text-slate-500 dark:text-slate-400">
                  <span>Remaining: <span className="font-semibold text-slate-700 dark:text-slate-200">${Math.max(bp.remaining, 0).toFixed(2)}</span></span>
                  <span>Alert at {bp.budget.alertPercentage}%</span>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create / Edit Modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
          <div className="bg-white dark:bg-dark-surface rounded-2xl shadow-2xl w-full max-w-md p-6 animate-in slide-in-from-bottom-4 duration-200">
            <div className="flex justify-between items-center mb-5">
              <h2 className="font-bold text-lg text-slate-800 dark:text-slate-100">
                {editId ? 'Edit Budget' : 'New Budget'}
              </h2>
              <button onClick={closeForm} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400">
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Category</label>
                <select
                  value={form.categoryId}
                  onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                  required
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                >
                  <option value="">Select a category</option>
                  {categories.map((c: any) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Monthly Limit ($)</label>
                <input
                  type="number" min="0.01" step="0.01"
                  value={form.monthlyLimit}
                  onChange={(e) => setForm({ ...form, monthlyLimit: e.target.value })}
                  placeholder="e.g. 500.00"
                  required
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                />
              </div>
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Alert Threshold (%)</label>
                <input
                  type="number" min="1" max="100"
                  value={form.alertPercentage}
                  onChange={(e) => setForm({ ...form, alertPercentage: e.target.value })}
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                />
                <p className="text-[11px] text-slate-400 mt-1">You'll get a notification when spending hits this %</p>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={closeForm} className="flex-1 py-2.5 rounded-xl border border-light-border dark:border-dark-border text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                  Cancel
                </button>
                <button type="submit" disabled={createBudget.isPending || updateBudget.isPending} className="flex-1 py-2.5 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 disabled:opacity-60 transition-colors">
                  {editId ? 'Save Changes' : 'Create Budget'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </motion.div>
  );
};
