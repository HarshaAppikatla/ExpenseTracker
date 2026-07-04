import { useQuery, useMutation, useQueryClient, UseQueryResult, UseMutationResult } from '@tanstack/react-query';
import { incomeService, Income, IncomeRequest } from '../services/incomeService';
import { PaginatedResponse } from '@/features/expense/services/expenseService';

export const useIncomeList = (page = 0, size = 20, sort = 'incomeDate,desc'): UseQueryResult<PaginatedResponse<Income>, Error> => {
  return useQuery({
    queryKey: ['income-list', page, size, sort],
    queryFn: () => incomeService.getIncomeList(page, size, sort),
  });
};

export const useIncome = (id: string, enabled = true): UseQueryResult<Income, Error> => {
  return useQuery({
    queryKey: ['income', id],
    queryFn: () => incomeService.getIncomeById(id),
    enabled: !!id && enabled,
  });
};

export const useCreateIncome = (): UseMutationResult<Income, Error, IncomeRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: IncomeRequest) => incomeService.createIncome(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['income-list'] });
    },
  });
};

export const useUpdateIncome = (): UseMutationResult<Income, Error, { id: string; data: IncomeRequest }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => incomeService.updateIncome(id, data),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['income-list'] });
      queryClient.setQueryData(['income', data.id], data);
    },
  });
};

export const useDeleteIncome = (): UseMutationResult<void, Error, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => incomeService.deleteIncome(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['income-list'] });
    },
  });
};
