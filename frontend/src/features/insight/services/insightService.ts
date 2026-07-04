import apiClient from '@/core/api/client';

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

export interface InsightDashboard {
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

export const insightService = {
  getInsights: async (): Promise<InsightDashboard> => {
    const res = await apiClient.get('/insights');
    return res.data.data;
  },
};
