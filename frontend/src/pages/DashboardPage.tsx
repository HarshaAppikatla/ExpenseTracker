import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useDashboardSummary, useFinancialDashboard } from '@/features/dashboard/hooks/useDashboard';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { useAuthContext } from '@/hooks/useAuthContext';
import { pageVariants } from '@/animations/variants';
import {
  TrendingUp,
  TrendingDown,
  Wallet,
  ArrowUpRight,
  ArrowDownRight,
  Target,
  PiggyBank,
  AlertTriangle,
  CheckCircle2,
  Calendar,
  Plus,
  Repeat2,
  Heart,
  X
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthContext();
  const { data: summary, isLoading: isSummaryLoading, isError: isSummaryError } = useDashboardSummary();
  const { data: financial, isLoading: isFinancialLoading, isError: isFinancialError } = useFinancialDashboard();
  const { data: profile } = useProfile();

  const [fabOpen, setFabOpen] = useState(false);

  const currencySymbol = profile?.preferredCurrency || '$';

  const isLoading = isSummaryLoading || isFinancialLoading;
  const isError = isSummaryError || isFinancialError;

  if (isLoading) {
    return <div className="text-center py-12 text-slate-500">Loading dashboard data...</div>;
  }

  if (isError || !summary || !financial) {
    return <div className="text-center py-12 text-red-500">Failed to load dashboard metrics.</div>;
  }

  const budgetUtilization = financial.budgetUtilizationRate;
  const budgetPct = Math.min(budgetUtilization, 100);
  const budgetColor =
    budgetUtilization >= 100
      ? 'bg-red-500 text-red-600 dark:text-red-400 border-red-200'
      : budgetUtilization >= 80
      ? 'bg-amber-500 text-amber-600 dark:text-amber-400 border-amber-200'
      : 'bg-emerald-500 text-emerald-600 dark:text-emerald-400 border-emerald-200';

  // Calculate savings rate
  const totalIncome = financial.totalIncomeCurrentMonth || 0;
  const netSavings = financial.netSavingsCurrentMonth || 0;
  const savingsRate = totalIncome > 0 ? (netSavings / totalIncome) * 100 : 0;

  // Calculate Financial Health Score (out of 100)
  let score = 0;
  // 1. Budget Adherence (30 points)
  if (budgetUtilization <= 80) score += 30;
  else if (budgetUtilization <= 100) score += 15;
  // 2. Savings Rate (40 points)
  if (savingsRate >= 20) score += 40;
  else if (savingsRate >= 10) score += 25;
  else if (savingsRate > 0) score += 10;
  // 3. Positive Net Balance (30 points)
  if (netSavings >= 0) score += 30;
  else if (Math.abs(netSavings) < totalIncome * 0.2) score += 15;

  const healthScore = Math.max(score, 15);
  const healthLabel = healthScore >= 80 ? 'Excellent' : healthScore >= 60 ? 'Healthy' : 'Needs Attention';
  const healthBadgeColor = healthScore >= 80 ? 'bg-emerald-500/10 text-emerald-500 border-emerald-500/25' : healthScore >= 60 ? 'bg-amber-500/10 text-amber-500 border-amber-500/25' : 'bg-red-500/10 text-red-500 border-red-500/25';

  return (
    <motion.div
      variants={pageVariants}
      initial="hidden"
      animate="visible"
      exit="exit"
      className="space-y-6 relative"
    >
      {/* Personalized Welcome Panel & Health Score Bento Header */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Welcome Card */}
        <div className="lg:col-span-2 bg-gradient-to-br from-slate-900 to-indigo-950 dark:from-slate-950 dark:to-indigo-950 p-6 rounded-2xl border border-indigo-900/30 text-white shadow-xl flex flex-col justify-between min-h-[160px]">
          <div>
            <h1 className="text-xl font-bold tracking-tight">Good Evening, {user?.fullName || 'User'} 👋</h1>
            <p className="text-xs text-indigo-300 mt-1">Here is a quick snapshot of your monthly financial planning:</p>
          </div>
          <div className="grid grid-cols-3 gap-4 pt-4 border-t border-indigo-900/30 text-center sm:text-left">
            <div>
              <span className="text-[10px] text-indigo-300 font-bold uppercase tracking-wider block">Budgets</span>
              <span className="text-xs font-semibold">{budgetUtilization >= 100 ? 'Exceeded' : budgetUtilization >= 80 ? 'Warning' : 'Healthy'}</span>
            </div>
            <div>
              <span className="text-[10px] text-indigo-300 font-bold uppercase tracking-wider block">Savings Rate</span>
              <span className="text-xs font-semibold">{savingsRate.toFixed(0)}% of income</span>
            </div>
            <div>
              <span className="text-[10px] text-indigo-300 font-bold uppercase tracking-wider block">Bill Schedules</span>
              <span className="text-xs font-semibold">Active planning rules</span>
            </div>
          </div>
        </div>

        {/* Financial Health Score Widget */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between min-h-[160px]">
          <div className="flex justify-between items-start">
            <div>
              <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Financial Health Score</span>
              <p className="text-[10px] text-slate-400 mt-0.5">Based on budget limits and savings ratios</p>
            </div>
            <div className={`p-2 rounded-xl bg-indigo-50 dark:bg-indigo-950/20 text-indigo-500`}>
              <Heart size={16} />
            </div>
          </div>
          <div className="flex items-center gap-4 mt-2">
            <span className="text-4xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">{healthScore}</span>
            <span className="text-sm text-slate-400">/ 100</span>
            <span className={`text-xs font-bold px-2.5 py-1 rounded-full border ${healthBadgeColor} ml-auto`}>
              {healthLabel}
            </span>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Total Net Balance Card */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between h-[150px]">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Total Net Balance</span>
            <div className="p-2 bg-indigo-50 dark:bg-indigo-950/20 rounded-xl text-indigo-500 border border-indigo-100 dark:border-indigo-950/40">
              <Wallet size={16} />
            </div>
          </div>
          <div>
            <h3 className="text-2xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">
              {currencySymbol} {summary.netBalance.toFixed(2)}
            </h3>
            <p className="text-[10px] text-slate-400 mt-1">
              Opening balance: {currencySymbol} {summary.openingBalance.toFixed(2)}
            </p>
          </div>
        </div>

        {/* Current Month Income Card */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between h-[150px]">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Monthly Income</span>
            <div className="p-2 bg-emerald-50 dark:bg-emerald-950/20 rounded-xl text-emerald-500 border border-emerald-100 dark:border-emerald-950/40">
              <TrendingUp size={16} />
            </div>
          </div>
          <div>
            <h3 className="text-2xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">
              + {currencySymbol} {financial.totalIncomeCurrentMonth.toFixed(2)}
            </h3>
            <p className="text-[10px] text-slate-400 mt-1">Deposits for current month</p>
          </div>
        </div>

        {/* Current Month Expenses Card */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between h-[150px]">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Monthly Spent</span>
            <div className="p-2 bg-rose-50 dark:bg-rose-950/20 rounded-xl text-rose-500 border border-rose-100 dark:border-rose-950/40">
              <TrendingDown size={16} />
            </div>
          </div>
          <div>
            <h3 className="text-2xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">
              - {currencySymbol} {financial.totalSpentCurrentMonth.toFixed(2)}
            </h3>
            <p className="text-[10px] text-slate-400 mt-1">Expenses for current month</p>
          </div>
        </div>

        {/* Monthly Net Savings Card */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between h-[150px]">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Net Savings</span>
            <div className="p-2 bg-indigo-50 dark:bg-indigo-950/20 rounded-xl text-indigo-500 border border-indigo-100 dark:border-indigo-950/40">
              <PiggyBank size={16} />
            </div>
          </div>
          <div>
            <h3 className={`text-2xl font-extrabold tracking-tight ${financial.netSavingsCurrentMonth >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
              {financial.netSavingsCurrentMonth >= 0 ? '+' : ''} {currencySymbol} {financial.netSavingsCurrentMonth.toFixed(2)}
            </h3>
            <p className="text-[10px] text-slate-400 mt-1">Income minus expenses this month</p>
          </div>
        </div>
      </div>

      {/* Main Grid: Insights and Trends / Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left column (2/3 width) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Monthly Trends Comparison */}
          {financial.monthlyTrends && financial.monthlyTrends.length > 0 && (
            <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm">
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-4 flex items-center gap-2">
                <Calendar size={16} className="text-slate-400" />
                Monthly Trends (Income vs. Expense)
              </h2>
              <div className="space-y-4">
                {financial.monthlyTrends.map((trend) => {
                  const maxAmount = Math.max(trend.income || 1, trend.expense || 1);
                  const incomePct = ((trend.income / maxAmount) * 100).toFixed(1);
                  const expensePct = ((trend.expense / maxAmount) * 100).toFixed(1);
                  return (
                    <div key={trend.monthName} className="space-y-1">
                      <div className="flex justify-between text-xs font-semibold">
                        <span className="text-slate-800 dark:text-slate-300">{trend.monthName}</span>
                        <span className="text-[11px] text-slate-400">
                          Inc: <span className="text-emerald-600">{currencySymbol}{trend.income.toFixed(0)}</span> &bull; Exp: <span className="text-rose-600">{currencySymbol}{trend.expense.toFixed(0)}</span>
                        </span>
                      </div>
                      <div className="space-y-1">
                        {/* Income bar */}
                        <div className="flex items-center gap-2">
                          <span className="text-[9px] w-6 text-slate-400 text-right">Inc</span>
                          <div className="flex-1 bg-slate-100 dark:bg-slate-800 h-1.5 rounded-full overflow-hidden">
                            <div className="bg-emerald-500 h-full rounded-full transition-all duration-500" style={{ width: `${incomePct}%` }} />
                          </div>
                        </div>
                        {/* Expense bar */}
                        <div className="flex items-center gap-2">
                          <span className="text-[9px] w-6 text-slate-400 text-right">Exp</span>
                          <div className="flex-1 bg-slate-100 dark:bg-slate-800 h-1.5 rounded-full overflow-hidden">
                            <div className="bg-rose-500 h-full rounded-full transition-all duration-500" style={{ width: `${expensePct}%` }} />
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Recent Activity */}
          <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm">
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-4">
              Recent Ledger Activity
            </h2>
            {!summary.recentTransactions.length ? (
              <div className="text-center py-8 text-xs text-slate-400">No transactions recorded.</div>
            ) : (
              <div className="flex flex-col divide-y divide-slate-100 dark:divide-slate-800">
                {summary.recentTransactions.map((tx) => (
                  <div key={tx.id} className="flex items-center justify-between py-3.5 first:pt-0 last:pb-0">
                    <div className="flex items-center gap-3">
                      <div
                        style={{
                          backgroundColor: `${tx.type === 'INCOME' ? '#2ECC71' : tx.categoryColor}15`,
                          color: tx.type === 'INCOME' ? '#2ECC71' : tx.categoryColor,
                        }}
                        className="w-9 h-9 rounded-xl flex items-center justify-center font-bold text-xs"
                      >
                        {tx.type === 'INCOME' ? <ArrowUpRight size={16} /> : <ArrowDownRight size={16} />}
                      </div>
                      <div>
                        <h4 className="text-xs font-bold text-slate-900 dark:text-slate-50">
                          {tx.sourceOrMerchant || 'Untitled'}
                        </h4>
                        <span className="text-[10px] text-slate-400">
                          {new Date(tx.date).toLocaleDateString()} &bull; {tx.categoryName || 'Income'}
                        </span>
                      </div>
                    </div>

                    <span
                      className={`text-xs font-bold ${
                        tx.type === 'INCOME' ? 'text-green-600 dark:text-green-400' : 'text-slate-900 dark:text-slate-50'
                      }`}
                    >
                      {tx.type === 'INCOME' ? '+' : '-'} {currencySymbol} {tx.amount.toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right column (1/3 width) */}
        <div className="space-y-6">
          {/* Overall Budget Status */}
          <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm">
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-3 flex items-center gap-2">
              <Target size={16} className="text-slate-400" />
              Budget Status
            </h2>
            {financial.budgetLimitTotal > 0 ? (
              <div className="space-y-3">
                <div className="flex justify-between text-xs">
                  <span className="text-slate-500 dark:text-slate-400">
                    Spent: <span className="font-semibold text-slate-800 dark:text-slate-200">{currencySymbol}{financial.budgetSpentTotal.toFixed(2)}</span>
                  </span>
                  <span className="text-slate-500 dark:text-slate-400">
                    Limit: <span className="font-semibold text-slate-800 dark:text-slate-200">{currencySymbol}{financial.budgetLimitTotal.toFixed(2)}</span>
                  </span>
                </div>
                <div className="h-2.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-700 ${budgetColor.split(' ')[0]}`}
                    style={{ width: `${budgetPct}%` }}
                  />
                </div>
                <div className="flex justify-between items-center text-xs">
                  <span className="text-slate-400">Utilization Rate</span>
                  <span className={`font-bold ${budgetColor.split(' ').slice(1).join(' ')}`}>
                    {financial.budgetUtilizationRate.toFixed(1)}%
                  </span>
                </div>
                {budgetUtilization >= 100 ? (
                  <div className="flex items-center gap-1.5 p-2.5 bg-red-50 dark:bg-red-950/20 border border-red-100 dark:border-red-900/40 rounded-xl text-red-600 dark:text-red-400 text-[10px]">
                    <AlertTriangle size={12} />
                    <span>Monthly budget exceeded! Adjust spending or update limits.</span>
                  </div>
                ) : budgetUtilization >= 80 ? (
                  <div className="flex items-center gap-1.5 p-2.5 bg-amber-50 dark:bg-amber-950/20 border border-amber-100 dark:border-amber-900/40 rounded-xl text-amber-600 dark:text-amber-400 text-[10px]">
                    <AlertTriangle size={12} />
                    <span>Warning: Approaching monthly budget limit.</span>
                  </div>
                ) : (
                  <div className="flex items-center gap-1.5 p-2.5 bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-950/40 rounded-xl text-emerald-600 dark:text-emerald-400 text-[10px]">
                    <CheckCircle2 size={12} />
                    <span>On track! Spending is well within monthly limits.</span>
                  </div>
                )}
              </div>
            ) : (
              <div className="text-center py-4 text-xs text-slate-400">
                No budget limit set for this month.
              </div>
            )}
          </div>

          {/* Savings Goals progress */}
          <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm">
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-3 flex items-center gap-2">
              <PiggyBank size={16} className="text-slate-400" />
              Savings Progress
            </h2>
            {!financial.savingsGoalsProgress || financial.savingsGoalsProgress.length === 0 ? (
              <div className="text-center py-4 text-xs text-slate-400">
                No active savings goals.
              </div>
            ) : (
              <div className="space-y-4">
                {financial.savingsGoalsProgress.map((goal) => {
                  const goalPct = Math.min(goal.progress, 100);
                  return (
                    <div key={goal.goalId} className="space-y-1">
                      <div className="flex justify-between text-xs font-semibold">
                        <span className="text-slate-800 dark:text-slate-300 truncate w-32">{goal.title}</span>
                        <span className="text-slate-900 dark:text-slate-100">
                          {goal.progress.toFixed(0)}%
                        </span>
                      </div>
                      <div className="h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                        <div className="bg-primary h-full rounded-full transition-all duration-500" style={{ width: `${goalPct}%` }} />
                      </div>
                      <div className="flex justify-between text-[9px] text-slate-400">
                        <span>Saved: {currencySymbol}{goal.currentAmount.toFixed(0)}</span>
                        <span>Target: {currencySymbol}{goal.targetAmount.toFixed(0)}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Top Spending Categories */}
          <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm">
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-3">
              Top Categories
            </h2>
            {!financial.topSpendingCategories || financial.topSpendingCategories.length === 0 ? (
              <div className="text-center py-4 text-xs text-slate-400">
                No categorical spending recorded.
              </div>
            ) : (
              <div className="space-y-3">
                {financial.topSpendingCategories.map((item) => (
                  <div key={item.categoryId} className="flex items-center justify-between text-xs">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                      <span className="text-slate-700 dark:text-slate-300 font-medium">{item.categoryName}</span>
                    </div>
                    <span className="font-bold text-slate-900 dark:text-slate-100">
                      {currencySymbol}{item.amount.toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Floating Speed Dial FAB */}
      <div className="fixed bottom-12 right-12 z-50 flex flex-col items-end gap-3">
        {fabOpen && (
          <div className="flex flex-col items-end gap-2 animate-in fade-in slide-in-from-bottom-6 duration-200">
            {[
              { label: 'New Expense', icon: TrendingDown, path: '/expenses', color: 'bg-rose-500 text-white' },
              { label: 'New Income', icon: TrendingUp, path: '/income', color: 'bg-emerald-500 text-white' },
              { label: 'Create Budget', icon: Target, path: '/budgets', color: 'bg-indigo-500 text-white' },
              { label: 'Create Recurring', icon: Repeat2, path: '/recurring', color: 'bg-amber-500 text-white' },
              { label: 'New Goal', icon: PiggyBank, path: '/savings', color: 'bg-violet-500 text-white' },
            ].map((act) => (
              <button
                key={act.label}
                onClick={() => { setFabOpen(false); navigate(act.path); }}
                className="flex items-center gap-3 px-4 py-2.5 rounded-xl shadow-lg border border-light-border dark:border-slate-800 bg-white dark:bg-dark-surface hover:bg-slate-50 dark:hover:bg-slate-800 transition-all transform hover:-translate-x-1"
              >
                <span className="text-[10px] font-bold text-slate-600 dark:text-slate-300">{act.label}</span>
                <div className={`p-2 rounded-lg ${act.color}`}>
                  <act.icon size={12} />
                </div>
              </button>
            ))}
          </div>
        )}
        <button
          onClick={() => setFabOpen(!fabOpen)}
          className={`w-12 h-12 rounded-full flex items-center justify-center text-white shadow-xl hover:scale-105 active:scale-95 transition-all ${
            fabOpen ? 'bg-slate-700 rotate-45' : 'bg-primary'
          }`}
        >
          {fabOpen ? <X size={20} /> : <Plus size={20} />}
        </button>
      </div>
    </motion.div>
  );
};

export default DashboardPage;
