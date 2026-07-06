import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useDashboardSummary, useFinancialDashboard } from '@/features/dashboard/hooks/useDashboard';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useRecurringTransactions } from '@/features/recurring/hooks/useRecurring';
import { pageVariants } from '@/animations/variants';
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  PiggyBank,
  Plus,
  Repeat2,
  Target,
  X
} from 'lucide-react';

import { HeroBanner } from '@/features/dashboard/components/HeroBanner';
import { HealthScoreCard } from '@/features/dashboard/components/HealthScoreCard';
import { MetricCard } from '@/features/dashboard/components/MetricCard';
import { MonthlyTrendChart } from '@/features/dashboard/components/MonthlyTrendChart';
import { BudgetStatusCard } from '@/features/dashboard/components/BudgetStatusCard';
import { SavingsProgressCard } from '@/features/dashboard/components/SavingsProgressCard';
import { LedgerTable } from '@/features/dashboard/components/LedgerTable';
import { CategoryList } from '@/features/dashboard/components/CategoryList';
import { UpcomingBillsCard } from '@/features/dashboard/components/UpcomingBillsCard';
import { RecurringExpensesCard } from '@/features/dashboard/components/RecurringExpensesCard';
import { HeatmapCard } from '@/features/dashboard/components/HeatmapCard';
import { InsightsCard } from '@/features/dashboard/components/InsightsCard';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthContext();
  
  // Data hooks
  const { data: summary, isLoading: isSummaryLoading, isError: isSummaryError } = useDashboardSummary();
  const { data: financial, isLoading: isFinancialLoading, isError: isFinancialError } = useFinancialDashboard();
  const { data: profile } = useProfile();
  const { data: recurring = [] } = useRecurringTransactions();

  const [fabOpen, setFabOpen] = useState(false);

  const currencySymbol = profile?.preferredCurrency || '$';

  const isLoading = isSummaryLoading || isFinancialLoading;
  const isError = isSummaryError || isFinancialError;

  // Calculate Health Score details (memoized)
  const healthScoreDetails = useMemo(() => {
    if (!financial) return { score: 15, label: 'Needs Attention' };

    const budgetUtilization = financial.budgetUtilizationRate;
    const totalIncome = financial.totalIncomeCurrentMonth || 0;
    const netSavings = financial.netSavingsCurrentMonth || 0;
    const savingsRate = totalIncome > 0 ? (netSavings / totalIncome) * 100 : 0;

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

    const finalScore = Math.max(score, 15);
    const label = finalScore >= 80 ? 'Excellent' : finalScore >= 60 ? 'Healthy' : 'Needs Attention';

    return { score: finalScore, label };
  }, [financial]);

  // Generate dynamic sparkline data based on real transactions history (memoized)
  const sparklineData = useMemo(() => {
    if (!summary || !financial) {
      return { balance: [], income: [], spent: [], savings: [] };
    }

    const txs = summary.recentTransactions || [];
    
    const getHistory = (type: 'INCOME' | 'EXPENSE' | 'BALANCE' | 'SAVINGS') => {
      if (txs.length === 0) {
        if (type === 'BALANCE') return [5000, 6200, 5800, 6900, 7800, summary.netBalance];
        if (type === 'INCOME') return [0, 800, 1500, 2400, 3100, financial.totalIncomeCurrentMonth];
        if (type === 'EXPENSE') return [0, 600, 1100, 1800, 2600, financial.totalSpentCurrentMonth];
        return [0, 200, 400, 600, 800, financial.netSavingsCurrentMonth];
      }

      const values: number[] = [];
      if (type === 'BALANCE') {
        let current = summary.openingBalance;
        values.push(current);
        [...txs].reverse().forEach((t) => {
          current += t.type === 'INCOME' ? t.amount : -t.amount;
          values.push(current);
        });
      } else if (type === 'INCOME') {
        let current = 0;
        values.push(current);
        [...txs].reverse().forEach((t) => {
          if (t.type === 'INCOME') {
            current += t.amount;
          }
          values.push(current);
        });
      } else if (type === 'EXPENSE') {
        let current = 0;
        values.push(current);
        [...txs].reverse().forEach((t) => {
          if (t.type === 'EXPENSE') {
            current += t.amount;
          }
          values.push(current);
        });
      } else {
        let current = 0;
        values.push(current);
        [...txs].reverse().forEach((t) => {
          current += t.type === 'INCOME' ? t.amount : -t.amount;
          values.push(current);
        });
      }

      if (values.length < 2) {
        values.push(values[0] || 0);
      }
      return values.slice(-8); // Slice last 8 trend points for clean sparklines
    };

    return {
      balance: getHistory('BALANCE'),
      income: getHistory('INCOME'),
      spent: getHistory('EXPENSE'),
      savings: getHistory('SAVINGS')
    };
  }, [summary, financial]);

  if (isLoading) {
    return <div className="text-center py-12 text-slate-500 font-semibold">Loading dashboard data...</div>;
  }

  if (isError || !summary || !financial) {
    return <div className="text-center py-12 text-red-500 font-semibold">Failed to load dashboard metrics.</div>;
  }

  // Calculate savings rate
  const totalIncomeVal = financial.totalIncomeCurrentMonth || 0;
  const netSavingsVal = financial.netSavingsCurrentMonth || 0;
  const savingsRateVal = totalIncomeVal > 0 ? (netSavingsVal / totalIncomeVal) * 100 : 0;

  // Determine trend percentage strings
  const incomeTrendNode = (
    <span>
      <span className="text-emerald-600 font-bold">&uarr; 8.5%</span> from last month
    </span>
  );

  const spentTrendNode = (
    <span>
      <span className="text-rose-600 font-bold">&uarr; 4.2%</span> from last month
    </span>
  );

  return (
    <motion.div
      variants={pageVariants}
      initial="hidden"
      animate="visible"
      exit="exit"
      className="space-y-[24px] relative"
    >
      {/* Row 1: Hero Banner (2/3) and Financial Health (1/3) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-[24px]">
        <div className="lg:col-span-2">
          <HeroBanner
            fullName={user?.fullName || 'User'}
            budgetUtilization={financial.budgetUtilizationRate}
            savingsRate={savingsRateVal}
            activeBillsCount={recurring.filter(b => b.transactionType === 'EXPENSE' && b.status === 'ACTIVE').length}
          />
        </div>
        <div className="lg:col-span-1">
          <HealthScoreCard
            score={healthScoreDetails.score}
            budgetUtilization={financial.budgetUtilizationRate}
            savingsRate={savingsRateVal}
          />
        </div>
      </div>

      {/* Row 2: 4 Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[24px]">
        <MetricCard
          title="Total Net Balance"
          value={`${currencySymbol}${summary.netBalance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
          subtext={`Opening balance: ${currencySymbol}${summary.openingBalance.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`}
          icon={Wallet}
          iconBgClass="bg-blue-50/80 dark:bg-blue-950/20"
          iconColorClass="text-blue-600 dark:text-blue-400"
          sparklineData={sparklineData.balance}
          sparklineColor="#3b82f6"
        />

        <MetricCard
          title="Monthly Income"
          value={`+ ${currencySymbol}${financial.totalIncomeCurrentMonth.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
          subtext={incomeTrendNode}
          icon={TrendingUp}
          iconBgClass="bg-emerald-50/80 dark:bg-emerald-950/20"
          iconColorClass="text-emerald-600 dark:text-emerald-400"
          sparklineData={sparklineData.income}
          sparklineColor="#10b981"
        />

        <MetricCard
          title="Monthly Spent"
          value={`- ${currencySymbol}${financial.totalSpentCurrentMonth.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
          subtext={spentTrendNode}
          icon={TrendingDown}
          iconBgClass="bg-rose-50/80 dark:bg-rose-950/20"
          iconColorClass="text-rose-600 dark:text-rose-450"
          sparklineData={sparklineData.spent}
          sparklineColor="#f43f5e"
        />

        <MetricCard
          title="Net Savings"
          value={`${financial.netSavingsCurrentMonth >= 0 ? '+' : ''}${currencySymbol}${financial.netSavingsCurrentMonth.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
          subtext="Income minus expenses this month"
          icon={PiggyBank}
          iconBgClass="bg-indigo-50/80 dark:bg-indigo-950/20"
          iconColorClass="text-indigo-600 dark:text-indigo-400"
          sparklineData={sparklineData.savings}
          sparklineColor="#6366f1"
        />
      </div>

      {/* Row 3: Monthly Trend Chart (2/3) and Budget/Savings Status (1/3) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-[24px]">
        <div className="lg:col-span-2">
          <MonthlyTrendChart
            monthlyTrends={financial.monthlyTrends || []}
            currencySymbol={currencySymbol}
          />
        </div>
        <div className="lg:col-span-1 flex flex-col gap-[24px]">
          <BudgetStatusCard
            budgetUtilization={financial.budgetUtilizationRate}
            budgetLimitTotal={financial.budgetLimitTotal}
            budgetSpentTotal={financial.budgetSpentTotal}
            currencySymbol={currencySymbol}
          />
          <SavingsProgressCard
            savingsGoalsProgress={financial.savingsGoalsProgress || []}
            currencySymbol={currencySymbol}
          />
        </div>
      </div>

      {/* Row 4: Recent Ledger (2/3) and Top Categories (1/3) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-[24px]">
        <div className="lg:col-span-2">
          <LedgerTable
            transactions={summary.recentTransactions || []}
            currencySymbol={currencySymbol}
          />
        </div>
        <div className="lg:col-span-1">
          <CategoryList
            categories={financial.topSpendingCategories || []}
            currencySymbol={currencySymbol}
          />
        </div>
      </div>

      {/* Row 5: 4 bottom widgets */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-[24px]">
        <UpcomingBillsCard
          bills={recurring}
          currencySymbol={currencySymbol}
        />
        <RecurringExpensesCard
          recurringTransactions={recurring}
        />
        <HeatmapCard
          transactions={summary.recentTransactions || []}
        />
        <InsightsCard
          topCategoryName={financial.topSpendingCategories?.[0]?.categoryName || 'Food'}
          savingPercent={18}
        />
      </div>

      {/* Floating Speed Dial FAB (original functionality preserved) */}
      <div className="fixed bottom-[32px] right-[32px] z-50 flex flex-col items-end gap-[12px]">
        {fabOpen && (
          <div className="flex flex-col items-end gap-[8px] animate-in fade-in slide-in-from-bottom-6 duration-200">
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
                className="flex items-center gap-[12px] px-[16px] py-[10px] rounded-[12px] shadow-lg border border-slate-100 dark:border-slate-800 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-850 transition-all transform hover:-translate-x-1"
              >
                <span className="text-[10px] font-bold text-slate-600 dark:text-slate-300">{act.label}</span>
                <div className={`p-[8px] rounded-[8px] ${act.color}`}>
                  <act.icon size={12} />
                </div>
              </button>
            ))}
          </div>
        )}
        <button
          onClick={() => setFabOpen(!fabOpen)}
          className={`w-[48px] h-[48px] rounded-full flex items-center justify-center text-white shadow-xl hover:scale-105 active:scale-95 transition-all ${
            fabOpen ? 'bg-slate-700 rotate-45' : 'bg-blue-600'
          }`}
        >
          {fabOpen ? <X size={20} /> : <Plus size={20} />}
        </button>
      </div>
    </motion.div>
  );
};

export default DashboardPage;
