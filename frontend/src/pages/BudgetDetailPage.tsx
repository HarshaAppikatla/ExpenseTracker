import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { useBudgetProgress, useDeleteBudget } from '@/features/budget/hooks/useBudget';
import { useExpenses } from '@/features/expense/hooks/useExpenses';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { matchCategory } from '@/features/expense/utils/categoryNormalizer';
import { WorkspaceLayout } from '@/layouts/WorkspaceLayout';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { ReusableBarChart } from '@/components/charts/ReusableBarChart';
import {
  TrendingDown,
  ArrowLeft,
  Trash2
} from 'lucide-react';
import toast from 'react-hot-toast';

export const BudgetDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: progress, isLoading: isProgressLoading, isError: isProgressError } = useBudgetProgress(id || '');
  const { data: expensesPage } = useExpenses(0, 100);
  const { data: profile } = useProfile();

  const deleteBudget = useDeleteBudget();

  const [isDeleting, setIsDeleting] = useState(false);

  const currencySymbol = profile?.preferredCurrency || '$';

  if (isProgressLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-24 space-y-4">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p className="text-sm text-slate-500">Loading budget details...</p>
      </div>
    );
  }

  if (isProgressError || !progress) {
    return (
      <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 p-6 rounded-2xl max-w-lg mx-auto my-12 text-center space-y-4">
        <h3 className="font-bold text-red-800 dark:text-red-400">Budget Not Found</h3>
        <p className="text-sm text-red-700 dark:text-red-500">
          The requested budget workspace could not be resolved.
        </p>
        <Button onClick={() => navigate('/budgets')} variant="outlined" className="mx-auto">
          Back to Budgets
        </Button>
      </div>
    );
  }

  const { budget, currentSpent, remaining, utilizationPercentage } = progress;
  const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  const monthName = MONTHS[budget.month - 1];

  const budgetExpenses = (expensesPage?.content || []).filter((exp) => {
    if (!matchCategory(exp.category, budget.categoryName)) return false;
    const expDate = new Date(exp.expenseDate);
    return expDate.getFullYear() === budget.year && (expDate.getMonth() + 1) === budget.month;
  });

  const weeklyData = [
    { name: 'Week 1', value: 0 },
    { name: 'Week 2', value: 0 },
    { name: 'Week 3', value: 0 },
    { name: 'Week 4', value: 0 },
  ];

  budgetExpenses.forEach((exp) => {
    const userSplit = exp.splits?.find(s => s.userId === profile?.id);
    const userOwed = userSplit ? userSplit.owedAmount : 0;
    const day = new Date(exp.expenseDate).getDate();
    if (day <= 7) weeklyData[0].value += userOwed;
    else if (day <= 14) weeklyData[1].value += userOwed;
    else if (day <= 21) weeklyData[2].value += userOwed;
    else weeklyData[3].value += userOwed;
  });

  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this budget limit?')) {
      setIsDeleting(true);
      deleteBudget.mutate(budget.id, {
        onSuccess: () => {
          toast.success('Budget deleted successfully');
          navigate('/budgets');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to delete budget');
          setIsDeleting(false);
        }
      });
    }
  };

  const isExceeded = utilizationPercentage >= 100;

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="w-full">
      <WorkspaceLayout
        title={`${budget.categoryName} Budget`}
        subtitle={`${monthName} ${budget.year} spending plan details`}
        action={
          <div className="flex gap-3">
            <Button onClick={() => navigate('/budgets')} variant="outlined" className="flex items-center gap-2">
              <ArrowLeft className="w-4 h-4" />
              <span>All Budgets</span>
            </Button>
            <Button onClick={handleDelete} disabled={isDeleting} className="bg-red-500 hover:bg-red-600 text-white flex items-center gap-2">
              <Trash2 className="w-4 h-4" />
              <span>Delete</span>
            </Button>
          </div>
        }
      >
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Card: Core Metrics & Status */}
          <div className="lg:col-span-1 space-y-6">
            <Card className="p-24 space-y-6">
              <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200">Plan Summary</h3>
              <div className="space-y-4">
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Status</span>
                  <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                    isExceeded ? 'bg-red-50 dark:bg-red-950/20 text-red-600' :
                    utilizationPercentage >= budget.alertPercentage ? 'bg-amber-50 dark:bg-amber-950/20 text-amber-600' :
                    'bg-emerald-50 dark:bg-emerald-950/20 text-emerald-600'
                  }`}>
                    {isExceeded ? 'Exceeded' : utilizationPercentage >= budget.alertPercentage ? 'Warning' : 'Healthy'}
                  </span>
                </div>
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Limit</span>
                  <span className="text-xs font-bold text-slate-800 dark:text-slate-200">{currencySymbol}{budget.monthlyLimit.toFixed(2)}</span>
                </div>
                <div className="flex justify-between border-b border-light-border dark:border-dark-border pb-3">
                  <span className="text-xs text-slate-400">Spent</span>
                  <span className="text-xs font-bold text-slate-800 dark:text-slate-200">{currencySymbol}{currentSpent.toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-xs text-slate-400">Remaining</span>
                  <span className={`text-xs font-bold ${remaining < 0 ? 'text-red-500' : 'text-emerald-500'}`}>{currencySymbol}{remaining.toFixed(2)}</span>
                </div>
              </div>

              {/* Progress bar */}
              <div className="space-y-2">
                <div className="flex justify-between text-[11px] font-semibold text-slate-500">
                  <span>Progress</span>
                  <span>{utilizationPercentage.toFixed(1)}%</span>
                </div>
                <div className="w-full h-2.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      isExceeded ? 'bg-red-500' : utilizationPercentage >= budget.alertPercentage ? 'bg-amber-500' : 'bg-emerald-500'
                    }`}
                    style={{ width: `${Math.min(utilizationPercentage, 100)}%` }}
                  />
                </div>
              </div>
            </Card>
          </div>

          {/* Right Cards: Weekly Graph & Recent Expenses */}
          <div className="lg:col-span-2 space-y-6">
            {/* Weekly chart */}
            <Card className="p-24">
              <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200 mb-6">Weekly Burn-down</h3>
              <div className="min-h-[220px]">
                <ReusableBarChart data={weeklyData} height={220} currencySymbol={currencySymbol} defaultColor={utilizationPercentage >= 100 ? '#EF4444' : '#2563EB'} />
              </div>
            </Card>

            {/* Category Expenses timeline */}
            <Card className="p-24 space-y-4">
              <h3 className="font-bold text-sm text-slate-800 dark:text-slate-200">Recent Category Expenses</h3>
              {!budgetExpenses.length ? (
                <div className="text-center py-12 text-xs text-slate-400">
                  No expenses logged under this category for {monthName} {budget.year}.
                </div>
              ) : (
                <div className="flex flex-col divide-y divide-slate-100 dark:divide-slate-800">
                  {budgetExpenses.map((exp) => {
                    const userSplit = exp.splits?.find(s => s.userId === profile?.id);
                    const userOwed = userSplit ? userSplit.owedAmount : 0;
                    return (
                      <div key={exp.id} className="flex justify-between items-center py-4 first:pt-0 last:pb-0">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-xl bg-rose-50 dark:bg-rose-950/20 text-rose-500 border border-rose-100 dark:border-rose-950/40 flex items-center justify-center font-bold text-xs">
                            <TrendingDown size={16} />
                          </div>
                          <div>
                            <h4 className="text-xs font-bold text-slate-900 dark:text-white">
                              {exp.description}
                            </h4>
                            <p className="text-[10px] text-slate-400">
                              {new Date(exp.expenseDate).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
                        <span className="text-xs font-bold text-slate-900 dark:text-white">
                          - {currencySymbol}{userOwed.toFixed(2)}
                        </span>
                      </div>
                    );
                  })}
                </div>
              )}
            </Card>
          </div>
        </div>
      </WorkspaceLayout>
    </motion.div>
  );
};
