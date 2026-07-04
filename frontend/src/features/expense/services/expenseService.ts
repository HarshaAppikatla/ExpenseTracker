import apiClient from '@/core/api/client';
import { Category } from '@/features/category/services/categoryService';

export interface Expense {
  id: string;
  userId: string;
  groupId?: string | null;
  category: Category;
  amount: number;
  currencyCode: string;
  expenseDate: string;
  merchant?: string;
  description?: string;
  notes?: string;
  latitude?: number;
  longitude?: number;
  locationName?: string;
  address?: string;
  status: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ExpenseRequest {
  categoryId: string;
  amount: number;
  currencyCode?: string;
  expenseDate: string;
  merchant?: string;
  description?: string;
  notes?: string;
  latitude?: number | null;
  longitude?: number | null;
  locationName?: string;
  address?: string;
  tags?: string[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export const expenseService = {
  getExpenses: async (page = 0, size = 20, sort = 'expenseDate,desc'): Promise<PaginatedResponse<Expense>> => {
    const res = await apiClient.get('/expenses', {
      params: { page, size, sort },
    });
    return res.data.data;
  },

  getExpenseById: async (id: string): Promise<Expense> => {
    const res = await apiClient.get(`/expenses/${id}`);
    return res.data.data;
  },

  createExpense: async (data: ExpenseRequest): Promise<Expense> => {
    const res = await apiClient.post('/expenses', data);
    return res.data.data;
  },

  updateExpense: async (id: string, data: ExpenseRequest): Promise<Expense> => {
    const res = await apiClient.put(`/expenses/${id}`, data);
    return res.data.data;
  },

  deleteExpense: async (id: string): Promise<void> => {
    await apiClient.delete(`/expenses/${id}`);
  },

  getMerchantSuggestions: async (query: string, limit = 10): Promise<string[]> => {
    if (!query.trim()) return [];
    const res = await apiClient.get('/expenses/merchants/suggestions', {
      params: { query, limit },
    });
    return res.data.data;
  },
};
