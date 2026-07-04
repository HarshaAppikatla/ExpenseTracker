import apiClient from '@/core/api/client';
import { Transaction } from '@/features/dashboard/services/dashboardService';
import { PaginatedResponse } from '@/features/expense/services/expenseService';

export interface TransactionFilterParams {
  type?: string;
  category?: string;
  sourceOrMerchant?: string;
  minAmount?: number;
  maxAmount?: number;
  fromDate?: string;
  toDate?: string;
  description?: string;
  tag?: string;
  page?: number;
  size?: number;
}

export const transactionService = {
  searchTransactions: async (params: TransactionFilterParams): Promise<PaginatedResponse<Transaction>> => {
    const cleanParams = Object.fromEntries(
      Object.entries(params).filter(([_, value]) => value !== undefined && value !== '')
    );
    const res = await apiClient.get('/transactions', { params: cleanParams });
    return res.data.data;
  },
};
