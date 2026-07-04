import { useMutation, useQueryClient } from '@tanstack/react-query';
import { groupCommands } from '../api';
import { groupKeys } from './groupKeys';
import toast from 'react-hot-toast';

export const useCreateGroup = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: groupCommands.createGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() });
      toast.success('Group created successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to create group';
      toast.error(msg);
    },
  });
};

export const useUpdateGroup = (id: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: any) => groupCommands.updateGroup(id, req),
    onSuccess: (data) => {
      queryClient.setQueryData(groupKeys.details(id), data);
      queryClient.invalidateQueries({ queryKey: groupKeys.details(id) });
      queryClient.invalidateQueries({ queryKey: groupKeys.dashboard(id) });
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() });
      toast.success('Group settings updated successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to update group';
      toast.error(msg);
    },
  });
};

export const useArchiveGroup = (id: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => groupCommands.archiveGroup(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.details(id) });
      queryClient.invalidateQueries({ queryKey: groupKeys.dashboard(id) });
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() });
      toast.success('Group archived successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to archive group';
      toast.error(msg);
    },
  });
};

export const useRestoreGroup = (id: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => groupCommands.restoreGroup(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.details(id) });
      queryClient.invalidateQueries({ queryKey: groupKeys.dashboard(id) });
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() });
      toast.success('Group restored successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to restore group';
      toast.error(msg);
    },
  });
};

export const useDeleteGroup = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: groupCommands.deleteGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() });
      toast.success('Group soft deleted successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to delete group';
      toast.error(msg);
    },
  });
};

export const useJoinGroup = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: groupCommands.joinGroup,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() });
      toast.success('Joined group successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to join group';
      toast.error(msg);
    },
  });
};

export const useLeaveGroup = (id: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => groupCommands.leaveGroup(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() });
      queryClient.removeQueries({ queryKey: groupKeys.details(id) });
      queryClient.removeQueries({ queryKey: groupKeys.dashboard(id) });
      toast.success('Left group successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to leave group';
      toast.error(msg);
    },
  });
};

export const useKickMember = (groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (memberId: string) => groupCommands.removeMember(groupId, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.dashboard(groupId) });
      queryClient.invalidateQueries({ queryKey: groupKeys.details(groupId) });
      toast.success('Member removed successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to remove member';
      toast.error(msg);
    },
  });
};

export const useUpdateRole = (groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (variables: { memberId: string; role: 'ADMIN' | 'MEMBER' }) =>
      groupCommands.updateMemberRole(groupId, variables.memberId, { role: variables.role }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.dashboard(groupId) });
      queryClient.invalidateQueries({ queryKey: groupKeys.details(groupId) });
      toast.success('Member role updated successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to update role';
      toast.error(msg);
    },
  });
};

export const useTransferOwnership = (groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (variables: { newOwnerId: string }) =>
      groupCommands.transferOwnership(groupId, { newOwnerId: variables.newOwnerId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: groupKeys.dashboard(groupId) });
      queryClient.invalidateQueries({ queryKey: groupKeys.details(groupId) });
      queryClient.invalidateQueries({ queryKey: groupKeys.lists() });
      toast.success('Group ownership transferred successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to transfer ownership';
      toast.error(msg);
    },
  });
};
