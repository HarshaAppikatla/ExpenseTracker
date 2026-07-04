import apiClient from '@/core/api/client';

// ─── Types ───────────────────────────────────────────────────────────────────

export interface RecurringTransaction {
  id: string;
  userId: string;
  transactionType: 'EXPENSE' | 'INCOME';
  categoryId?: string;
  categoryName?: string;
  amount: number;
  currencyCode: string;
  merchant?: string;
  description?: string;
  recurrenceType: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
  recurrenceInterval: number;
  startDate: string;
  nextExecution: string;
  endDate?: string;
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'EXPIRED';
}

export interface RecurringRequest {
  transactionType: 'EXPENSE' | 'INCOME';
  categoryId?: string;
  amount: number;
  currencyCode?: string;
  merchant?: string;
  description?: string;
  recurrenceType: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
  recurrenceInterval: number;
  startDate: string;
  endDate?: string;
}

// ─── Service ──────────────────────────────────────────────────────────────────

export const recurringService = {
  getAll: async (): Promise<RecurringTransaction[]> => {
    const res = await apiClient.get('/recurring');
    return res.data.data;
  },

  create: async (data: RecurringRequest): Promise<RecurringTransaction> => {
    const res = await apiClient.post('/recurring', data);
    return res.data.data;
  },

  update: async (id: string, data: RecurringRequest): Promise<RecurringTransaction> => {
    const res = await apiClient.put(`/recurring/${id}`, data);
    return res.data.data;
  },

  pause: async (id: string): Promise<void> => {
    await apiClient.put(`/recurring/${id}/pause`);
  },

  resume: async (id: string): Promise<void> => {
    await apiClient.put(`/recurring/${id}/resume`);
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/recurring/${id}`);
  },
};
