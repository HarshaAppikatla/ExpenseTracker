import { useQuery, useMutation, useQueryClient, UseQueryResult, UseMutationResult } from '@tanstack/react-query';
import { expenseService, Expense, ExpenseRequest, PaginatedResponse } from '../services/expenseService';

export const useExpenses = (page = 0, size = 20, sort = 'expenseDate,desc'): UseQueryResult<PaginatedResponse<Expense>, Error> => {
  return useQuery({
    queryKey: ['expenses', page, size, sort],
    queryFn: () => expenseService.getExpenses(page, size, sort),
  });
};

export const useExpense = (id: string, enabled = true): UseQueryResult<Expense, Error> => {
  return useQuery({
    queryKey: ['expense', id],
    queryFn: () => expenseService.getExpenseById(id),
    enabled: !!id && enabled,
  });
};

export const useCreateExpense = (): UseMutationResult<Expense, Error, ExpenseRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ExpenseRequest) => expenseService.createExpense(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
    },
  });
};

export const useUpdateExpense = (): UseMutationResult<Expense, Error, { id: string; data: ExpenseRequest }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => expenseService.updateExpense(id, data),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
      queryClient.setQueryData(['expense', data.id], data);
    },
  });
};

export const useDeleteExpense = (): UseMutationResult<void, Error, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => expenseService.deleteExpense(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
    },
  });
};

export const useMerchantSuggestions = (query: string, enabled = true): UseQueryResult<string[], Error> => {
  return useQuery({
    queryKey: ['merchants', 'suggestions', query],
    queryFn: () => expenseService.getMerchantSuggestions(query),
    enabled: !!query && enabled,
    staleTime: 60 * 1000,
  });
};
