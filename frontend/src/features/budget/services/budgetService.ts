import apiClient from '@/core/api/client';

// ─── Types ───────────────────────────────────────────────────────────────────

export interface Budget {
  id: string;
  userId: string;
  categoryId: string;
  categoryName: string;
  year: number;
  month: number;
  monthlyLimit: number;
  currencyCode: string;
  alertPercentage: number;
  active: boolean;
}

export interface BudgetProgress {
  budget: Budget;
  currentSpent: number;
  remaining: number;
  utilizationPercentage: number;
}

export interface BudgetRequest {
  categoryId: string;
  year: number;
  month: number;
  monthlyLimit: number;
  currencyCode?: string;
  alertPercentage?: number;
}

// ─── Service ──────────────────────────────────────────────────────────────────

export const budgetService = {
  getBudgets: async (year: number, month: number): Promise<BudgetProgress[]> => {
    const res = await apiClient.get('/budgets', { params: { year, month } });
    return res.data.data;
  },

  createBudget: async (data: BudgetRequest): Promise<Budget> => {
    const res = await apiClient.post('/budgets', data);
    return res.data.data;
  },

  updateBudget: async (id: string, data: BudgetRequest): Promise<Budget> => {
    const res = await apiClient.put(`/budgets/${id}`, data);
    return res.data.data;
  },

  deleteBudget: async (id: string): Promise<void> => {
    await apiClient.delete(`/budgets/${id}`);
  },

  getBudgetProgress: async (id: string): Promise<BudgetProgress> => {
    const res = await apiClient.get(`/budgets/${id}/progress`);
    return res.data.data;
  },
};
