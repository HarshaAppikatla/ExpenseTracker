import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import {
  expenseService,
  GroupExpense,
  CreateGroupExpenseRequest,
  UpdateGroupExpenseRequest,
  GroupExpenseStatus,
  StorageProvider,
} from '../services/expenseService';

export const useCreateGroupExpense = (groupId: string): UseMutationResult<GroupExpense, Error, CreateGroupExpenseRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateGroupExpenseRequest) => expenseService.createGroupExpense(groupId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group-expenses', groupId] });
    },
  });
};

export const useUpdateGroupExpense = (groupId: string): UseMutationResult<GroupExpense, Error, { id: string; data: UpdateGroupExpenseRequest }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => expenseService.updateGroupExpense(id, data),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['group-expenses', groupId] });
      queryClient.setQueryData(['group-expense', data.id], data);
    },
  });
};

export const useDeleteGroupExpense = (groupId: string): UseMutationResult<void, Error, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => expenseService.deleteGroupExpense(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['group-expenses', groupId] });
    },
  });
};

export const useTransitionGroupExpenseStatus = (groupId: string): UseMutationResult<GroupExpense, Error, { id: string; status: GroupExpenseStatus }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }) => expenseService.transitionStatus(id, status),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['group-expenses', groupId] });
      queryClient.setQueryData(['group-expense', data.id], data);
    },
  });
};

export const useAddGroupExpenseAttachment = (groupId: string): UseMutationResult<
  GroupExpense,
  Error,
  {
    expenseId: string;
    url: string;
    fileName: string;
    fileSize: number;
    fileType: string;
    storageProvider: StorageProvider;
  }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ expenseId, ...params }) => expenseService.addAttachment(expenseId, params),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['group-expenses', groupId] });
      queryClient.setQueryData(['group-expense', data.id], data);
    },
  });
};
