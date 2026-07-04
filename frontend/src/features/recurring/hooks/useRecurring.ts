import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { recurringService, RecurringRequest } from '../services/recurringService';

export const useRecurringTransactions = () =>
  useQuery({
    queryKey: ['recurring'],
    queryFn: () => recurringService.getAll(),
    staleTime: 30_000,
  });

export const useCreateRecurring = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: RecurringRequest) => recurringService.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['recurring'] }),
  });
};

export const useUpdateRecurring = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: RecurringRequest }) =>
      recurringService.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['recurring'] }),
  });
};

export const usePauseRecurring = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => recurringService.pause(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['recurring'] }),
  });
};

export const useResumeRecurring = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => recurringService.resume(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['recurring'] }),
  });
};

export const useDeleteRecurring = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => recurringService.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['recurring'] }),
  });
};

export const useRecurringById = (id: string) => {
  const query = useRecurringTransactions();
  const template = query.data?.find((t) => t.id === id);
  return {
    ...query,
    data: template,
  } as any;
};
