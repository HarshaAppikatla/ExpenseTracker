import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { savingsService, SavingsGoalRequest, SavingsDepositRequest } from '../services/savingsService';

export const useSavingsGoals = () =>
  useQuery({
    queryKey: ['savings-goals'],
    queryFn: () => savingsService.getGoals(),
    staleTime: 30_000,
  });

export const useCreateSavingsGoal = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: SavingsGoalRequest) => savingsService.createGoal(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['savings-goals'] }),
  });
};

export const useUpdateSavingsGoal = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: SavingsGoalRequest }) =>
      savingsService.updateGoal(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['savings-goals'] }),
  });
};

export const useDeleteSavingsGoal = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => savingsService.deleteGoal(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['savings-goals'] }),
  });
};

export const useAddSavingsDeposit = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ goalId, data }: { goalId: string; data: SavingsDepositRequest }) =>
      savingsService.addDeposit(goalId, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['savings-goals'] });
      qc.invalidateQueries({ queryKey: ['savings-deposits'] });
    },
  });
};

export const useSavingsById = (id: string) => {
  const query = useSavingsGoals();
  const goal = query.data?.find((g) => g.id === id);
  return {
    ...query,
    data: goal,
  } as any;
};

export const useSavingsDeposits = (goalId: string) =>
  useQuery({
    queryKey: ['savings-deposits', goalId],
    queryFn: () => savingsService.getDeposits(goalId),
    staleTime: 30_000,
    enabled: !!goalId,
  });
