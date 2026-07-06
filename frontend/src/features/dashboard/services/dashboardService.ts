import apiClient from '@/core/api/client';

export interface CategoryBreakdown {
  name: string;
  color: string;
  amount: number;
}

export interface Transaction {
  id: string;
  type: 'EXPENSE' | 'INCOME';
  amount: number;
  currencyCode: string;
  date: string;
  categoryName?: string;
  categoryColor?: string;
  sourceOrMerchant?: string;
  description?: string;
  tags?: string[];
}

export interface DashboardSummary {
  openingBalance: number;
  totalIncome: number;
  totalExpenses: number;
  netBalance: number;
  categoryBreakdown: CategoryBreakdown[];
  recentTransactions: Transaction[];
}

export interface CategorySpend {
  categoryId: string;
  categoryName: string;
  amount: number;
  color: string;
}

export interface MonthlyTrend {
  monthName: string;
  income: number;
  expense: number;
}

export interface SavingsGoalProgress {
  goalId: string;
  title: string;
  targetAmount: number;
  currentAmount: number;
  progress: number;
}

export interface FinancialDashboard {
  totalSpentCurrentMonth: number;
  totalIncomeCurrentMonth: number;
  netSavingsCurrentMonth: number;
  budgetLimitTotal: number;
  budgetSpentTotal: number;
  budgetUtilizationRate: number;
  topSpendingCategories: CategorySpend[];
  monthlyTrends: MonthlyTrend[];
  savingsGoalsProgress: SavingsGoalProgress[];
}

export const dashboardService = {
  getSummary: async (): Promise<DashboardSummary> => {
    const res = await apiClient.get('/dashboard/summary');
    return res.data.data;
  },
  getFinancialDashboard: async (): Promise<FinancialDashboard> => {
    const res = await apiClient.get('/dashboard/financial');
    return res.data.data;
  },
};

