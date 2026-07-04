import apiClient from '@/core/api/client';

// ─── Types ───────────────────────────────────────────────────────────────────

export interface SavingsGoal {
  id: string;
  userId: string;
  title: string;
  description?: string;
  targetAmount: number;
  targetDate?: string;
  currentAmount: number;
  progressPercentage: number;
  completed: boolean;
  completedAt?: string;
}

export interface SavingsDeposit {
  id: string;
  goalId: string;
  amount: number;
  depositDate: string;
  notes?: string;
}

export interface SavingsGoalRequest {
  title: string;
  description?: string;
  targetAmount: number;
  targetDate?: string;
}

export interface SavingsDepositRequest {
  amount: number;
  notes?: string;
}

// ─── Service ──────────────────────────────────────────────────────────────────

export const savingsService = {
  getGoals: async (): Promise<SavingsGoal[]> => {
    const res = await apiClient.get('/savings');
    return res.data.data;
  },

  createGoal: async (data: SavingsGoalRequest): Promise<SavingsGoal> => {
    const res = await apiClient.post('/savings', data);
    return res.data.data;
  },

  updateGoal: async (id: string, data: SavingsGoalRequest): Promise<SavingsGoal> => {
    const res = await apiClient.put(`/savings/${id}`, data);
    return res.data.data;
  },

  deleteGoal: async (id: string): Promise<void> => {
    await apiClient.delete(`/savings/${id}`);
  },

  addDeposit: async (goalId: string, data: SavingsDepositRequest): Promise<SavingsDeposit> => {
    const res = await apiClient.post(`/savings/${goalId}/deposits`, data);
    return res.data.data;
  },

  getDeposits: async (goalId: string): Promise<SavingsDeposit[]> => {
    const res = await apiClient.get(`/savings/${goalId}/deposits`);
    return res.data.data;
  },
};
