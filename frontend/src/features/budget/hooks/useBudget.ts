import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { budgetService, BudgetRequest } from '../services/budgetService';

const QUERY_KEY = (year: number, month: number) => ['budgets', year, month];

export const useBudgets = (year: number, month: number) =>
  useQuery({
    queryKey: QUERY_KEY(year, month),
    queryFn: () => budgetService.getBudgets(year, month),
    staleTime: 30_000,
  });

export const useCreateBudget = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: BudgetRequest) => budgetService.createBudget(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  });
};

export const useUpdateBudget = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: BudgetRequest }) =>
      budgetService.updateBudget(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  });
};

export const useDeleteBudget = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => budgetService.deleteBudget(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  });
};

export const useBudgetProgress = (id: string) =>
  useQuery({
    queryKey: ['budget-progress', id],
    queryFn: () => budgetService.getBudgetProgress(id),
    staleTime: 30_000,
    enabled: !!id,
  });
