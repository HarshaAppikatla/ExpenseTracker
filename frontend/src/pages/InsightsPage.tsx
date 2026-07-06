import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { pageVariants } from '@/animations/variants';
import { useInsights } from '@/features/insight/hooks/useInsight';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { useExpenses } from '@/features/expense/hooks/useExpenses';
import { matchCategory } from '@/features/expense/utils/categoryNormalizer';
import {
  TrendingUp,
  TrendingDown,
  Target,
  PiggyBank,
  PieChart as PieIcon,
  LineChart as LineIcon,
  Activity,
  AlertCircle,
  ArrowLeft
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell
} from 'recharts';

export const InsightsPage: React.FC = () => {
  const { data: insights, isLoading, isError } = useInsights();
  const { data: profile } = useProfile();
  const { data: expensesPage } = useExpenses(0, 100);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  const currencySymbol = profile?.preferredCurrency || '$';

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-24 space-y-4">
        <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
        <p className="text-sm text-slate-500 dark:text-slate-400">Analyzing financial data and loading insights...</p>
      </div>
    );
  }

  if (isError || !insights) {
    return (
      <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/40 p-6 rounded-2xl flex items-start gap-4 max-w-2xl mx-auto my-12">
        <AlertCircle className="w-6 h-6 text-red-500 shrink-0 mt-1" />
        <div>
          <h3 className="font-bold text-red-800 dark:text-red-400">Failed to Load Insights</h3>
          <p className="text-sm text-red-700 dark:text-red-500 mt-1">
            An unexpected error occurred while loading your financial insights. Please try again.
          </p>
        </div>
      </div>
    );
  }

  // Aggregate stats calculations
  const totalIncome = insights.totalIncomeCurrentMonth || 0;
  const totalSpent = insights.totalSpentCurrentMonth || 0;
  const netSavings = insights.netSavingsCurrentMonth || 0;
  const savingsRate = totalIncome > 0 ? ((netSavings / totalIncome) * 100) : 0;
  const budgetSpent = insights.budgetSpentTotal || 0;
  const budgetLimit = insights.budgetLimitTotal || 0;
  const budgetUtilization = insights.budgetUtilizationRate || 0;

  // Custom tooltips for Recharts
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border shadow-lg text-xs space-y-1">
          <p className="font-bold text-slate-900 dark:text-slate-100">{label}</p>
          {payload.map((p: any) => (
            <p key={p.name} style={{ color: p.color || p.fill }} className="font-semibold">
              {p.name}: {currencySymbol}{Number(p.value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  const CustomPieTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="bg-white dark:bg-dark-surface p-4 rounded-xl border border-light-border dark:border-dark-border shadow-lg text-xs">
          <p className="font-bold text-slate-900 dark:text-slate-100">{data.categoryName}</p>
          <p className="font-semibold mt-1" style={{ color: data.color }}>
            Amount: {currencySymbol}{Number(data.amount).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <motion.div variants={pageVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50 flex items-center gap-3">
          <Activity className="w-6 h-6 text-primary" />
          Financial Insights
        </h1>
        <p className="text-xs text-slate-400">Actionable intelligence, planning trends, and deep analytics</p>
      </div>

      {/* Bento Grid: Core Aggregates */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Income Card */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between h-[150px]">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Monthly Income</span>
            <div className="p-2 bg-emerald-50 dark:bg-emerald-950/20 rounded-xl text-emerald-500 border border-emerald-100 dark:border-emerald-950/40">
              <TrendingUp size={16} />
            </div>
          </div>
          <div>
            <h3 className="text-2xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">
              {currencySymbol} {totalIncome.toLocaleString(undefined, { minimumFractionDigits: 2 })}
            </h3>
            <p className="text-[10px] text-slate-400 mt-1">Total deposits for the current month</p>
          </div>
        </div>

        {/* Expenses Card */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between h-[150px]">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Monthly Spent</span>
            <div className="p-2 bg-rose-50 dark:bg-rose-950/20 rounded-xl text-rose-500 border border-rose-100 dark:border-rose-950/40">
              <TrendingDown size={16} />
            </div>
          </div>
          <div>
            <h3 className="text-2xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">
              {currencySymbol} {totalSpent.toLocaleString(undefined, { minimumFractionDigits: 2 })}
            </h3>
            <p className="text-[10px] text-slate-400 mt-1">Total expenses for the current month</p>
          </div>
        </div>

        {/* Savings Rate Card */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between h-[150px]">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Savings Rate</span>
            <div className="p-2 bg-indigo-50 dark:bg-indigo-950/20 rounded-xl text-indigo-500 border border-indigo-100 dark:border-indigo-950/40">
              <PiggyBank size={16} />
            </div>
          </div>
          <div>
            <h3 className={`text-2xl font-extrabold tracking-tight ${netSavings >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
              {savingsRate >= 0 ? '+' : ''} {savingsRate.toFixed(1)} %
            </h3>
            <p className="text-[10px] text-slate-400 mt-1">
              Net savings: {currencySymbol} {netSavings.toLocaleString(undefined, { minimumFractionDigits: 2 })}
            </p>
          </div>
        </div>

        {/* Budget Utilization Card */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col justify-between h-[150px]">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-500 dark:text-slate-400">Budget Limit Used</span>
            <div className="p-2 bg-amber-50 dark:bg-amber-950/20 rounded-xl text-amber-500 border border-amber-100 dark:border-amber-950/40">
              <Target size={16} />
            </div>
          </div>
          <div>
            <h3 className="text-2xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">
              {budgetUtilization.toFixed(1)} %
            </h3>
            <p className="text-[10px] text-slate-400 mt-1">
              {currencySymbol}{budgetSpent.toLocaleString(undefined, { maximumFractionDigits: 0 })} used of {currencySymbol}{budgetLimit.toLocaleString(undefined, { maximumFractionDigits: 0 })}
            </p>
          </div>
        </div>
      </div>

      {/* Main Charts Bento Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Monthly Trend Area Chart (2/3 width) */}
        <div className="lg:col-span-2 bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col">
          <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-4 flex items-center gap-2">
            <LineIcon className="w-4 h-4 text-primary" />
            Income vs. Expenses Trend
          </h2>
          <div className="flex-1 min-h-[300px] w-full">
            {insights.monthlyTrends && insights.monthlyTrends.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={insights.monthlyTrends} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorIncome" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10B981" stopOpacity={0.2}/>
                      <stop offset="95%" stopColor="#10B981" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="colorExpense" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#F43F5E" stopOpacity={0.2}/>
                      <stop offset="95%" stopColor="#F43F5E" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="monthName" stroke="#94A3B8" fontSize={11} tickLine={false} axisLine={false} />
                  <YAxis stroke="#94A3B8" fontSize={11} tickLine={false} axisLine={false} />
                  <Tooltip content={<CustomTooltip />} />
                  <Legend verticalAlign="top" height={36} iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 11 }} />
                  <Area name="Income" type="monotone" dataKey="income" stroke="#10B981" strokeWidth={2} fillOpacity={1} fill="url(#colorIncome)" />
                  <Area name="Expenses" type="monotone" dataKey="expense" stroke="#F43F5E" strokeWidth={2} fillOpacity={1} fill="url(#colorExpense)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-[300px] text-xs text-slate-400">
                No historical trends found. Start logging transactions to view charts.
              </div>
            )}
          </div>
        </div>

        {/* Top Spending Categories Pie Chart (1/3 width) */}
        <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm flex flex-col">
          <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-4 flex items-center gap-2">
            <PieIcon className="w-4 h-4 text-primary" />
            Top Spending Categories
          </h2>
          <div className="flex-1 flex flex-col justify-center min-h-[300px]">
            {selectedCategory ? (
              (() => {
                const catInfo = insights.topSpendingCategories.find((c) => c.categoryId === selectedCategory);
                const catExpenses = (expensesPage?.content || []).filter(
                  (exp) => matchCategory(exp.category, catInfo?.categoryName || '')
                );
                return (
                  <div className="space-y-4">
                    <div className="flex items-center gap-2 border-b border-light-border dark:border-dark-border pb-3">
                      <button
                        onClick={() => setSelectedCategory(null)}
                        className="p-1 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500"
                        title="Back to breakdown"
                      >
                        <ArrowLeft size={16} />
                      </button>
                      <div>
                        <h3 className="text-xs font-bold text-slate-900 dark:text-white">
                          {catInfo?.categoryName || 'Category Details'}
                        </h3>
                        <p className="text-[10px] text-slate-400">
                          Total category spent: {currencySymbol}{catInfo?.amount.toFixed(2)}
                        </p>
                      </div>
                    </div>

                    <div className="space-y-3 max-h-[220px] overflow-y-auto pr-1">
                      {catExpenses.length === 0 ? (
                        <p className="text-center py-8 text-[11px] text-slate-400">No expenses logged for this category.</p>
                      ) : (
                        catExpenses.map((exp) => {
                          const userSplit = exp.splits?.find(s => s.userId === profile?.id);
                          const userOwed = userSplit ? userSplit.owedAmount : 0;
                          return (
                            <div key={exp.id} className="flex justify-between items-center text-xs">
                              <div>
                                <p className="font-bold text-slate-850 dark:text-slate-100">
                                  {exp.description || 'Unspecified Expense'}
                                </p>
                                <span className="text-[9px] text-slate-450">
                                  {new Date(exp.expenseDate).toLocaleDateString()}
                                </span>
                              </div>
                              <span className="font-bold text-slate-900 dark:text-white">
                                -{currencySymbol}{userOwed.toFixed(2)}
                              </span>
                            </div>
                          );
                        })
                      )}
                    </div>
                  </div>
                );
              })()
            ) : insights.topSpendingCategories && insights.topSpendingCategories.length > 0 ? (
              <>
                <div className="h-[200px] w-full relative flex items-center justify-center">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Tooltip content={<CustomPieTooltip />} />
                      <Pie
                        data={insights.topSpendingCategories}
                        cx="50%"
                        cy="50%"
                        innerRadius={50}
                        outerRadius={75}
                        paddingAngle={3}
                        dataKey="amount"
                        onClick={(data) => {
                          if (data && data.categoryId) {
                            setSelectedCategory(data.categoryId);
                          }
                        }}
                        className="cursor-pointer"
                      >
                        {insights.topSpendingCategories.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color || '#6366F1'} />
                        ))}
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                  {/* Total spent label in center */}
                  <div className="absolute flex flex-col items-center justify-center">
                    <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Total Spent</span>
                    <span className="text-md font-extrabold text-slate-800 dark:text-slate-100">
                      {currencySymbol}{totalSpent.toLocaleString(undefined, { maximumFractionDigits: 0 })}
                    </span>
                  </div>
                </div>
                {/* List of categories */}
                <div className="space-y-2 mt-4 max-h-[120px] overflow-y-auto pr-1">
                  {insights.topSpendingCategories.map((category) => (
                    <div
                      key={category.categoryId}
                      onClick={() => setSelectedCategory(category.categoryId)}
                      className="flex justify-between items-center text-xs cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-850 p-1.5 rounded-xl transition-colors"
                    >
                      <div className="flex items-center gap-2 min-w-0">
                        <span style={{ backgroundColor: category.color }} className="w-2.5 h-2.5 rounded-full shrink-0" />
                        <span className="text-slate-700 dark:text-slate-300 truncate font-semibold">{category.categoryName}</span>
                      </div>
                      <span className="text-slate-950 dark:text-white font-bold shrink-0">
                        {currencySymbol}{category.amount.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <div className="flex items-center justify-center h-[260px] text-xs text-slate-400">
                No category breakdown available.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Savings Goal Progress list */}
      <div className="bg-white dark:bg-dark-surface p-6 rounded-2xl border border-light-border dark:border-dark-border shadow-sm">
        <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-6 flex items-center gap-2">
          <Target className="w-4 h-4 text-primary" />
          Active Savings Goals Progress
        </h2>
        {insights.savingsGoalsProgress && insights.savingsGoalsProgress.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {insights.savingsGoalsProgress.map((goal) => {
              const goalPct = Math.min(goal.progress || 0, 100);
              return (
                <div key={goal.goalId} className="bg-slate-50 dark:bg-slate-900/50 p-5 rounded-2xl border border-slate-150 dark:border-slate-800 flex flex-col justify-between h-[130px]">
                  <div>
                    <h3 className="text-xs font-bold text-slate-950 dark:text-white truncate">{goal.title}</h3>
                    <div className="flex justify-between items-baseline mt-2 text-[10px] text-slate-400">
                      <span>Saved: <span className="font-bold text-slate-700 dark:text-slate-300">{currencySymbol}{goal.currentAmount.toLocaleString(undefined, { maximumFractionDigits: 0 })}</span></span>
                      <span>Target: <span className="font-bold text-slate-700 dark:text-slate-300">{currencySymbol}{goal.targetAmount.toLocaleString(undefined, { maximumFractionDigits: 0 })}</span></span>
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <div className="flex justify-between items-center text-[10px] font-semibold text-slate-500">
                      <span>Progress</span>
                      <span>{goal.progress.toFixed(0)} %</span>
                    </div>
                    <div className="bg-slate-200 dark:bg-slate-800 h-2 rounded-full overflow-hidden w-full">
                      <div className="bg-indigo-500 h-full rounded-full transition-all duration-500" style={{ width: `${goalPct}%` }} />
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="text-center py-12 text-xs text-slate-400">
            No active savings goals found. Set up savings goals under the Savings module!
          </div>
        )}
      </div>

      {/* Insight Explanation Cards Feed */}
      <div className="space-y-4">
        <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
          <Activity className="w-4 h-4 text-primary animate-pulse" />
          Financial Advisory Feed
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Savings rate advisory */}
          <div className={`p-5 rounded-2xl border ${
            savingsRate >= 20 ? 'bg-emerald-50/40 dark:bg-emerald-950/10 border-emerald-200 dark:border-emerald-900/30' :
            savingsRate >= 10 ? 'bg-blue-50/40 dark:bg-blue-950/10 border-blue-200 dark:border-blue-900/30' :
            'bg-rose-50/40 dark:bg-rose-950/10 border-rose-200 dark:border-rose-900/30'
          }`}>
            <div className="flex justify-between items-start mb-3">
              <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200">Savings Cushion</h3>
              <PiggyBank className={`w-4 h-4 ${
                savingsRate >= 20 ? 'text-emerald-500' : savingsRate >= 10 ? 'text-blue-500' : 'text-rose-500'
              }`} />
            </div>
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              {savingsRate >= 20 ? 'Excellent! You saved a substantial portion of your earnings. Keep building your wealth.' :
               savingsRate >= 10 ? 'Healthy saving activity. Look into optimizing subscription templates to reach the 20% mark.' :
               'Alert: Low savings cushion. Consider adding stricter budget categories to protect your cashflow.'}
            </p>
          </div>

          {/* Budget advisory */}
          <div className={`p-5 rounded-2xl border ${
            budgetUtilization >= 100 ? 'bg-rose-50/40 dark:bg-rose-950/10 border-rose-200 dark:border-rose-900/30' :
            budgetUtilization >= 80 ? 'bg-amber-50/40 dark:bg-amber-950/10 border-amber-200 dark:border-amber-900/30' :
            'bg-emerald-50/40 dark:bg-emerald-950/10 border-emerald-200 dark:border-emerald-900/30'
          }`}>
            <div className="flex justify-between items-start mb-3">
              <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200">Budget Adherence</h3>
              <Target className={`w-4 h-4 ${
                budgetUtilization >= 100 ? 'text-rose-500' : budgetUtilization >= 80 ? 'text-amber-500' : 'text-emerald-500'
              }`} />
            </div>
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              {budgetUtilization >= 100 ? 'Critical: You have fully exceeded your monthly budget. Review individual categories immediately.' :
               budgetUtilization >= 80 ? 'Warning: You are approaching your total limit threshold. Cool down discretionary spending.' :
               'Awesome job! Your monthly limits are safely guarded and utilization is normal.'}
            </p>
          </div>

          {/* Cashflow advisory */}
          <div className={`p-5 rounded-2xl border ${
            netSavings >= 0 ? 'bg-emerald-50/40 dark:bg-emerald-950/10 border-emerald-200 dark:border-emerald-900/30' :
            'bg-rose-50/40 dark:bg-rose-950/10 border-rose-200 dark:border-rose-900/30'
          }`}>
            <div className="flex justify-between items-start mb-3">
              <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200">Cashflow Health</h3>
              <TrendingUp className={`w-4 h-4 ${netSavings >= 0 ? 'text-emerald-500' : 'text-rose-500'}`} />
            </div>
            <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              {netSavings >= 0 ? 'Positive net balance. Consider auto-depositing some of these funds directly into active savings goals.' :
               'Negative net cashflow detected. You spent more than you earned. Pause non-essential recurring plans.'}
            </p>
          </div>
        </div>
      </div>
    </motion.div>
  );
};
