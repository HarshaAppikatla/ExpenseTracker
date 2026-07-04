import apiClient from '@/core/api/client';
import { PaginatedResponse } from '@/features/expense/services/expenseService';

export interface Income {
  id: string;
  userId: string;
  amount: number;
  currencyCode: string;
  source: string;
  incomeDate: string;
  description?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface IncomeRequest {
  amount: number;
  currencyCode?: string;
  source: string;
  incomeDate: string;
  description?: string;
  notes?: string;
}

export const incomeService = {
  getIncomeList: async (page = 0, size = 20, sort = 'incomeDate,desc'): Promise<PaginatedResponse<Income>> => {
    const res = await apiClient.get('/income', {
      params: { page, size, sort },
    });
    return res.data.data;
  },

  getIncomeById: async (id: string): Promise<Income> => {
    const res = await apiClient.get(`/income/${id}`);
    return res.data.data;
  },

  createIncome: async (data: IncomeRequest): Promise<Income> => {
    const res = await apiClient.post('/income', data);
    return res.data.data;
  },

  updateIncome: async (id: string, data: IncomeRequest): Promise<Income> => {
    const res = await apiClient.put(`/income/${id}`, data);
    return res.data.data;
  },

  deleteIncome: async (id: string): Promise<void> => {
    await apiClient.delete(`/income/${id}`);
  },
};
