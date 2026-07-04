import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { receiptService, Receipt } from '../services/receiptService';

export const useUploadReceipt = (): UseMutationResult<Receipt, Error, { expenseId: string; file: File }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ expenseId, file }) => receiptService.uploadReceipt(expenseId, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
    },
  });
};

export const useReplaceReceipt = (): UseMutationResult<Receipt, Error, { receiptId: string; expenseId: string; file: File }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ receiptId, expenseId, file }) => receiptService.replaceReceipt(receiptId, expenseId, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
    },
  });
};

export const useDeleteReceipt = (): UseMutationResult<void, Error, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (receiptId: string) => receiptService.deleteReceipt(receiptId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
    },
  });
};
