import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { transactionService, TransactionFilterParams } from '../services/transactionService';
import { Transaction } from '@/features/dashboard/services/dashboardService';
import { PaginatedResponse } from '@/features/expense/services/expenseService';

export const useTransactions = (params: TransactionFilterParams): UseQueryResult<PaginatedResponse<Transaction>, Error> => {
  return useQuery({
    queryKey: ['transactions-search', params],
    queryFn: () => transactionService.searchTransactions(params),
  });
};
