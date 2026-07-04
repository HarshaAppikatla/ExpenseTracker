import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  useSavingsGoals, useCreateSavingsGoal, useUpdateSavingsGoal,
  useDeleteSavingsGoal, useAddSavingsDeposit
} from '@/features/savings/hooks/useSavings';
import { SavingsGoalRequest, SavingsDepositRequest, SavingsGoal } from '@/features/savings/services/savingsService';
import {
  PlusCircle, Trash2, Edit2, X, Target, CheckCircle2,
  PiggyBank, TrendingUp, Plus, Calendar
} from 'lucide-react';

const fmtDate = (d?: string) => d ? new Date(d).toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' }) : '—';

interface GoalFormState { title: string; description: string; targetAmount: string; targetDate: string; }
const emptyGoalForm: GoalFormState = { title: '', description: '', targetAmount: '', targetDate: '' };

interface DepositFormState { amount: string; notes: string; }
const emptyDepositForm: DepositFormState = { amount: '', notes: '' };

export const SavingsPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { data: goals = [], isLoading } = useSavingsGoals();
  const createGoal = useCreateSavingsGoal();
  const updateGoal = useUpdateSavingsGoal();
  const deleteGoal = useDeleteSavingsGoal();
  const addDeposit = useAddSavingsDeposit();

  const [showGoalForm, setShowGoalForm] = useState(location.pathname.endsWith('/new'));
  const [editGoal, setEditGoal] = useState<SavingsGoal | null>(null);
  const [goalForm, setGoalForm] = useState<GoalFormState>(emptyGoalForm);

  const closeGoalForm = () => {
    setShowGoalForm(false);
    if (location.pathname.endsWith('/new')) {
      navigate('/savings');
    }
  };

  const [depositTarget, setDepositTarget] = useState<SavingsGoal | null>(null);
  const [depositForm, setDepositForm] = useState<DepositFormState>(emptyDepositForm);

  const openCreate = () => { setEditGoal(null); setGoalForm(emptyGoalForm); setShowGoalForm(true); };
  const openEdit = (g: SavingsGoal) => {
    setEditGoal(g);
    setGoalForm({
      title: g.title,
      description: g.description || '',
      targetAmount: String(g.targetAmount),
      targetDate: g.targetDate ? g.targetDate.substring(0, 10) : '',
    });
    setShowGoalForm(true);
  };

  const handleGoalSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const payload: SavingsGoalRequest = {
      title: goalForm.title,
      description: goalForm.description || undefined,
      targetAmount: parseFloat(goalForm.targetAmount),
      targetDate: goalForm.targetDate ? goalForm.targetDate + 'T00:00:00' : undefined,
    };
    if (editGoal) {
      updateGoal.mutate({ id: editGoal.id, data: payload }, { onSuccess: () => { closeGoalForm(); setEditGoal(null); } });
    } else {
      createGoal.mutate(payload, { onSuccess: () => closeGoalForm() });
    }
  };

  const handleDeposit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!depositTarget) return;
    const payload: SavingsDepositRequest = {
      amount: parseFloat(depositForm.amount),
      notes: depositForm.notes || undefined,
    };
    addDeposit.mutate({ goalId: depositTarget.id, data: payload }, {
      onSuccess: () => { setDepositTarget(null); setDepositForm(emptyDepositForm); },
    });
  };

  const totalTarget = goals.reduce((s, g) => s + g.targetAmount, 0);
  const totalSaved = goals.reduce((s, g) => s + g.currentAmount, 0);
  const completedCount = goals.filter((g) => g.completed).length;

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Savings Goals</h1>
          <p className="text-xs text-slate-400 mt-0.5">Track your financial goals and celebrate milestones</p>
        </div>
        <button
          onClick={openCreate}
          className="inline-flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-xl text-sm font-semibold hover:bg-primary/90 active:scale-95 transition-all shadow-md shadow-primary/20"
        >
          <PlusCircle size={16} /> New Goal
        </button>
      </div>

      {/* KPI Summary */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[
          { icon: PiggyBank, label: 'Total Saved', value: `$${totalSaved.toFixed(2)}`, color: 'emerald' },
          { icon: Target, label: 'Total Target', value: `$${totalTarget.toFixed(2)}`, color: 'indigo' },
          { icon: CheckCircle2, label: 'Goals Completed', value: `${completedCount} / ${goals.length}`, color: 'violet' },
        ].map((kpi) => (
          <div key={kpi.label} className="bg-white dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-2xl p-5">
            <div className="flex justify-between items-start">
              <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">{kpi.label}</span>
              <div className={`p-2 rounded-xl bg-${kpi.color}-50 dark:bg-${kpi.color}-950/20 text-${kpi.color}-500`}>
                <kpi.icon size={16} />
              </div>
            </div>
            <p className="text-2xl font-extrabold text-slate-900 dark:text-slate-50 mt-2">{kpi.value}</p>
          </div>
        ))}
      </div>

      {/* Goals Grid */}
      {isLoading ? (
        <div className="text-center py-16 text-slate-400">Loading goals...</div>
      ) : goals.length === 0 ? (
        <div className="flex flex-col items-center gap-3 py-20">
          <div className="w-16 h-16 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
            <PiggyBank size={28} className="text-slate-400" />
          </div>
          <p className="text-slate-600 dark:text-slate-300 font-semibold">No savings goals yet</p>
          <p className="text-sm text-slate-400">Create a goal to start tracking your savings journey.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {goals.map((goal) => {
            const pct = Math.min(goal.progressPercentage, 100);
            return (
              <div key={goal.id} className={`bg-white dark:bg-dark-surface border rounded-2xl p-5 hover:shadow-md transition-shadow ${goal.completed ? 'border-emerald-200 dark:border-emerald-900/40' : 'border-light-border dark:border-dark-border'}`}>
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 cursor-pointer hover:underline" onClick={() => navigate(`/savings/${goal.id}`)}>
                      <span className="font-bold text-slate-800 dark:text-slate-100">{goal.title}</span>
                      {goal.completed && (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold bg-emerald-100 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 px-2 py-0.5 rounded-full">
                          <CheckCircle2 size={10} /> Completed
                        </span>
                      )}
                    </div>
                    {goal.description && (
                      <p className="text-xs text-slate-400 mt-0.5 line-clamp-1">{goal.description}</p>
                    )}
                  </div>
                  <div className="flex gap-1 ml-2">
                    <button onClick={() => { setDepositTarget(goal); setDepositForm(emptyDepositForm); }} className="p-2 rounded-xl hover:bg-emerald-50 dark:hover:bg-emerald-950/20 text-slate-400 hover:text-emerald-600 transition-colors" title="Add deposit">
                      <Plus size={14} />
                    </button>
                    <button onClick={() => openEdit(goal)} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 hover:text-slate-600 transition-colors">
                      <Edit2 size={14} />
                    </button>
                    <button onClick={() => deleteGoal.mutate(goal.id)} className="p-2 rounded-xl hover:bg-red-50 dark:hover:bg-red-950/20 text-slate-400 hover:text-red-500 transition-colors">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>

                {/* Progress */}
                <div className="mb-3">
                  <div className="flex justify-between text-xs mb-1.5">
                    <span className="text-slate-500 dark:text-slate-400">${goal.currentAmount.toFixed(2)} saved</span>
                    <span className="font-bold text-primary">{goal.progressPercentage.toFixed(1)}%</span>
                  </div>
                  <div className="h-2.5 bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-700 ${goal.completed ? 'bg-emerald-500' : 'bg-primary'}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>

                <div className="flex justify-between text-xs text-slate-500 dark:text-slate-400">
                  <span className="flex items-center gap-1">
                    <TrendingUp size={11} />
                    Target: <span className="font-semibold text-slate-700 dark:text-slate-300">${goal.targetAmount.toFixed(2)}</span>
                  </span>
                  {goal.targetDate && (
                    <span className="flex items-center gap-1">
                      <Calendar size={11} />
                      {fmtDate(goal.targetDate)}
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Goal Form Modal */}
      {showGoalForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
          <div className="bg-white dark:bg-dark-surface rounded-2xl shadow-2xl w-full max-w-md p-6">
            <div className="flex justify-between items-center mb-5">
              <h2 className="font-bold text-lg text-slate-800 dark:text-slate-100">
                {editGoal ? 'Edit Goal' : 'New Savings Goal'}
              </h2>
              <button onClick={closeGoalForm} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400">
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleGoalSubmit} className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Goal Title</label>
                <input
                  value={goalForm.title}
                  onChange={(e) => setGoalForm({ ...goalForm, title: e.target.value })}
                  placeholder="e.g. Emergency Fund, Dream Vacation"
                  required
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                />
              </div>
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Description (optional)</label>
                <input
                  value={goalForm.description}
                  onChange={(e) => setGoalForm({ ...goalForm, description: e.target.value })}
                  placeholder="A short description"
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                />
              </div>
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Target Amount ($)</label>
                <input
                  type="number" min="0.01" step="0.01"
                  value={goalForm.targetAmount}
                  onChange={(e) => setGoalForm({ ...goalForm, targetAmount: e.target.value })}
                  placeholder="e.g. 5000.00"
                  required
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                />
              </div>
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Target Date (optional)</label>
                <input
                  type="date"
                  value={goalForm.targetDate}
                  onChange={(e) => setGoalForm({ ...goalForm, targetDate: e.target.value })}
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={closeGoalForm} className="flex-1 py-2.5 rounded-xl border border-light-border dark:border-dark-border text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">
                  Cancel
                </button>
                <button type="submit" disabled={createGoal.isPending || updateGoal.isPending} className="flex-1 py-2.5 rounded-xl bg-primary text-white text-sm font-semibold hover:bg-primary/90 disabled:opacity-60 transition-colors">
                  {editGoal ? 'Save Changes' : 'Create Goal'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Deposit Modal */}
      {depositTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
          <div className="bg-white dark:bg-dark-surface rounded-2xl shadow-2xl w-full max-w-sm p-6">
            <div className="flex justify-between items-center mb-4">
              <div>
                <h2 className="font-bold text-lg text-slate-800 dark:text-slate-100">Add Deposit</h2>
                <p className="text-xs text-slate-400">{depositTarget.title}</p>
              </div>
              <button onClick={() => setDepositTarget(null)} className="p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400">
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleDeposit} className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Amount ($)</label>
                <input
                  type="number" min="0.01" step="0.01"
                  value={depositForm.amount}
                  onChange={(e) => setDepositForm({ ...depositForm, amount: e.target.value })}
                  placeholder="e.g. 100.00"
                  required
                  autoFocus
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                />
              </div>
              <div>
                <label className="text-xs font-semibold text-slate-600 dark:text-slate-300 block mb-1">Notes (optional)</label>
                <input
                  value={depositForm.notes}
                  onChange={(e) => setDepositForm({ ...depositForm, notes: e.target.value })}
                  placeholder="e.g. Monthly transfer"
                  className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-sm bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                />
              </div>
              <div className="flex gap-3">
                <button type="button" onClick={() => setDepositTarget(null)} className="flex-1 py-2.5 rounded-xl border border-light-border dark:border-dark-border text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 transition-colors">
                  Cancel
                </button>
                <button type="submit" disabled={addDeposit.isPending} className="flex-1 py-2.5 rounded-xl bg-emerald-600 text-white text-sm font-semibold hover:bg-emerald-700 disabled:opacity-60 transition-colors">
                  Add Deposit
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </motion.div>
  );
};
