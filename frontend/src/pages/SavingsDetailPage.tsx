import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { useSavingsById, useSavingsDeposits, useAddSavingsDeposit, useDeleteSavingsGoal } from '@/features/savings/hooks/useSavings';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { WorkspaceLayout } from '@/layouts/WorkspaceLayout';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { ReusableBarChart } from '@/components/charts/ReusableBarChart';
import {
  ArrowLeft,
  AlertTriangle,
  PiggyBank,
  CheckCircle2,
  Trash2,
  PlusCircle,
  Circle
} from 'lucide-react';
import toast from 'react-hot-toast';

export const SavingsDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: goal, isLoading: isGoalLoading, isError: isGoalError } = useSavingsById(id || '');
  const { data: deposits = [], isLoading: isDepositsLoading } = useSavingsDeposits(id || '');
  const addDeposit = useAddSavingsDeposit();
  const deleteGoal = useDeleteSavingsGoal();

  const { data: profile } = useProfile();

  const [showDepositModal, setShowDepositModal] = useState(false);
  const [depositAmount, setDepositAmount] = useState('');
  const [depositNotes, setDepositNotes] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);

  const currencySymbol = profile?.preferredCurrency || '$';

  if (isGoalLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-24 space-y-4">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p className="text-sm text-slate-500">Loading savings goal details...</p>
      </div>
    );
  }

  if (isGoalError || !goal) {
    return (
      <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 p-6 rounded-2xl max-w-lg mx-auto my-12 text-center space-y-4">
        <AlertTriangle className="w-8 h-8 text-red-500 mx-auto" />
        <h3 className="font-bold text-red-800 dark:text-red-400">Goal Not Found</h3>
        <p className="text-sm text-red-700 dark:text-red-500">
          The requested savings goal details could not be resolved.
        </p>
        <Button onClick={() => navigate('/savings')} variant="outlined" className="mx-auto">
          Back to Goals
        </Button>
      </div>
    );
  }

  const remaining = Math.max(goal.targetAmount - goal.currentAmount, 0);
  const pct = Math.min(goal.progressPercentage, 100);

  // Milestones
  const milestones = [
    { label: 'Start', pct: 0, hit: true },
    { label: 'Quarter Way', pct: 25, hit: goal.progressPercentage >= 25 },
    { label: 'Half Way', pct: 50, hit: goal.progressPercentage >= 50 },
    { label: 'Three Quarters', pct: 75, hit: goal.progressPercentage >= 75 },
    { label: 'Fully Funded', pct: 100, hit: goal.progressPercentage >= 100 },
  ];

  const handleDepositSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsed = parseFloat(depositAmount);
    if (isNaN(parsed) || parsed <= 0) {
      toast.error('Please enter a positive deposit amount');
      return;
    }
    addDeposit.mutate(
      { goalId: goal.id, data: { amount: parsed, notes: depositNotes || undefined } },
      {
        onSuccess: () => {
          toast.success('Savings deposit added successfully');
          setShowDepositModal(false);
          setDepositAmount('');
          setDepositNotes('');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to add deposit');
        }
      }
    );
  };

  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this savings goal?')) {
      setIsDeleting(true);
      deleteGoal.mutate(goal.id, {
        onSuccess: () => {
          toast.success('Savings goal deleted');
          navigate('/savings');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to delete goal');
          setIsDeleting(false);
        }
      });
    }
  };

  // Chart data: past deposits
  const chartData = deposits
    .slice()
    .reverse()
    .map((dep, idx) => ({
      name: `Dep ${idx + 1}`,
      value: dep.amount,
    }));

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="w-full">
      <WorkspaceLayout
        title={goal.title}
        subtitle={goal.description || 'Target savings workspace'}
        action={
          <div className="flex gap-3">
            <Button onClick={() => navigate('/savings')} variant="outlined" className="flex items-center gap-2">
              <ArrowLeft className="w-4 h-4" />
              <span>All Goals</span>
            </Button>
            <Button onClick={() => setShowDepositModal(true)} className="bg-primary hover:bg-primary/95 text-white flex items-center gap-2">
              <PlusCircle className="w-4 h-4" />
              <span>Add Deposit</span>
            </Button>
            <Button onClick={handleDelete} disabled={isDeleting} className="bg-red-500 hover:bg-red-600 text-white flex items-center gap-2">
              <Trash2 className="w-4 h-4" />
              <span>Delete</span>
            </Button>
          </div>
        }
      >
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Side: Summary Metrics & Milestones */}
          <div className="space-y-6 lg:col-span-1">
            <Card className="p-24 space-y-6">
              <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200">Goal Summary</h3>
              <div className="space-y-4">
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Target Amount</span>
                  <span className="text-xs font-extrabold text-slate-800 dark:text-slate-100">
                    {currencySymbol} {goal.targetAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                  </span>
                </div>
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Total Saved</span>
                  <span className="text-xs font-extrabold text-emerald-600">
                    {currencySymbol} {goal.currentAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                  </span>
                </div>
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Remaining</span>
                  <span className="text-xs font-extrabold text-slate-800 dark:text-slate-100">
                    {currencySymbol} {remaining.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-xs text-slate-400">Target Date</span>
                  <span className="text-xs font-semibold text-slate-800 dark:text-slate-100">
                    {goal.targetDate ? new Date(goal.targetDate).toLocaleDateString() : 'None'}
                  </span>
                </div>
              </div>

              {/* Progress Bar */}
              <div className="space-y-2 pt-2">
                <div className="flex justify-between items-center text-xs">
                  <span className="font-semibold text-slate-500">Savings Progress</span>
                  <span className="font-bold text-slate-900 dark:text-white">{goal.progressPercentage.toFixed(1)}%</span>
                </div>
                <div className="w-full bg-slate-100 dark:bg-slate-800 h-2.5 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${goal.completed ? 'bg-emerald-500' : 'bg-primary'}`}
                    style={{ width: `${pct}%` }}
                  />
                </div>
              </div>
            </Card>

            {/* Milestone Checklist */}
            <Card className="p-24 space-y-4">
              <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200">Milestone Tracker</h3>
              <div className="space-y-4 pt-2">
                {milestones.map((m) => (
                  <div key={m.label} className="flex items-center gap-3">
                    {m.hit ? (
                      <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0" />
                    ) : (
                      <Circle className="w-5 h-5 text-slate-300 shrink-0" />
                    )}
                    <span className={`text-xs font-semibold ${m.hit ? 'text-slate-900 dark:text-slate-100 font-bold' : 'text-slate-400'}`}>
                      {m.label} ({m.pct}%)
                    </span>
                  </div>
                ))}
              </div>
            </Card>
          </div>

          {/* Right Side: Deposit History & Charts */}
          <div className="lg:col-span-2 space-y-6">
            {/* Chart */}
            {chartData.length > 0 && (
              <Card className="p-24">
                <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200 mb-6">Deposit Trend</h3>
                <div className="min-h-[200px]">
                  <ReusableBarChart data={chartData} height={200} currencySymbol={currencySymbol} defaultColor="#10B981" />
                </div>
              </Card>
            )}

            {/* Deposit Ledger */}
            <Card className="p-24 space-y-4">
              <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200">Deposit Ledger</h3>
              {isDepositsLoading ? (
                <div className="text-center py-8 text-xs text-slate-400">Loading ledger...</div>
              ) : !deposits.length ? (
                <div className="text-center py-12 text-xs text-slate-400">
                  No deposits recorded. Add money to watch this goal grow!
                </div>
              ) : (
                <div className="flex flex-col divide-y divide-slate-100 dark:divide-slate-800">
                  {deposits.map((dep) => (
                    <div key={dep.id} className="flex justify-between items-center py-4 first:pt-0 last:pb-0">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-xl bg-emerald-50 dark:bg-emerald-950/20 text-emerald-500 border border-emerald-100 dark:border-emerald-950/40 flex items-center justify-center font-bold text-xs">
                          <PiggyBank size={16} />
                        </div>
                        <div>
                          <h4 className="text-xs font-bold text-slate-900 dark:text-white">
                            {dep.notes || 'Savings Deposit'}
                          </h4>
                          <p className="text-[10px] text-slate-400 font-medium">
                            {new Date(dep.depositDate).toLocaleDateString()}
                          </p>
                        </div>
                      </div>
                      <span className="text-xs font-bold text-emerald-600">
                        + {currencySymbol}{dep.amount.toFixed(2)}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </div>
        </div>

        {/* Add Deposit Dialog */}
        {showDepositModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
            <div className="bg-white dark:bg-dark-surface rounded-2xl shadow-2xl w-full max-w-md p-6">
              <div className="flex justify-between items-center mb-5">
                <h2 className="font-bold text-lg text-slate-800 dark:text-slate-100">Add Savings Deposit</h2>
                <button onClick={() => setShowDepositModal(false)} className="text-slate-400 hover:text-slate-600">
                  &times;
                </button>
              </div>
              <form onSubmit={handleDepositSubmit} className="space-y-4">
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    Deposit Amount ({currencySymbol})
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    required
                    placeholder="0.00"
                    value={depositAmount}
                    onChange={(e) => setDepositAmount(e.target.value)}
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-xs bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                  />
                </div>
                <div>
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">Notes</label>
                  <input
                    type="text"
                    placeholder="e.g. Monthly transfer, Piggy bank refill"
                    value={depositNotes}
                    onChange={(e) => setDepositNotes(e.target.value)}
                    className="w-full border border-light-border dark:border-dark-border rounded-xl px-3 py-2.5 text-xs bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/50"
                  />
                </div>
                <div className="flex justify-end gap-3 pt-3">
                  <Button type="button" onClick={() => setShowDepositModal(false)} variant="outlined">
                    Cancel
                  </Button>
                  <Button type="submit" className="bg-primary hover:bg-primary/95 text-white">
                    Add Deposit
                  </Button>
                </div>
              </form>
            </div>
          </div>
        )}
      </WorkspaceLayout>
    </motion.div>
  );
};
