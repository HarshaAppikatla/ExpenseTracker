import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { settlementApi } from '../api/settlementApi';
import { settlementKeys } from './settlementKeys';
import { ConfirmSettlementRequest, DisputeSettlementRequest } from '../types';
import toast from 'react-hot-toast';

export const useSettlementSummary = (groupId: string, enabled = true) => {
  return useQuery({
    queryKey: settlementKeys.summary(groupId),
    queryFn: () => settlementApi.getSettlementSummary(groupId),
    enabled: !!groupId && enabled,
  });
};

export const useMySettlements = (groupId: string, enabled = true) => {
  return useQuery({
    queryKey: settlementKeys.mySettlements(groupId),
    queryFn: () => settlementApi.getMySettlements(groupId),
    enabled: !!groupId && enabled,
  });
};

export const useSettlementsByTrip = (
  groupId: string,
  tripId: string,
  page = 0,
  size = 10,
  enabled = true
) => {
  return useQuery({
    queryKey: settlementKeys.tripList(groupId, tripId, page),
    queryFn: () => settlementApi.getSettlementsByTrip(groupId, tripId, page, size),
    enabled: !!groupId && !!tripId && enabled,
  });
};

export const useGenerateSettlements = (groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (tripId?: string) => settlementApi.generateSettlements(groupId, tripId),
    onSuccess: (data) => {
      queryClient.setQueryData(settlementKeys.summary(groupId), data);
      queryClient.invalidateQueries({ queryKey: settlementKeys.summary(groupId) });
      queryClient.invalidateQueries({ queryKey: settlementKeys.mySettlements(groupId) });
      toast.success('Settlements generated and simplified successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to generate settlements';
      toast.error(msg);
    },
  });
};

export const useConfirmPayment = (groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (variables: { id: string; request: ConfirmSettlementRequest }) =>
      settlementApi.confirmPayment(variables.id, variables.request),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: settlementKeys.summary(groupId) });
      queryClient.invalidateQueries({ queryKey: settlementKeys.mySettlements(groupId) });
      queryClient.invalidateQueries({ queryKey: settlementKeys.detail(data.id) });
      toast.success('Payment confirmed successfully!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to confirm payment';
      toast.error(msg);
    },
  });
};

export const useDisputeSettlement = (groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (variables: { id: string; request: DisputeSettlementRequest }) =>
      settlementApi.disputeSettlement(variables.id, variables.request),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: settlementKeys.summary(groupId) });
      queryClient.invalidateQueries({ queryKey: settlementKeys.mySettlements(groupId) });
      queryClient.invalidateQueries({ queryKey: settlementKeys.detail(data.id) });
      toast.success('Dispute raised successfully.');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to raise dispute';
      toast.error(msg);
    },
  });
};

export const useResolveSettlement = (groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => settlementApi.resolveSettlement(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: settlementKeys.summary(groupId) });
      queryClient.invalidateQueries({ queryKey: settlementKeys.mySettlements(groupId) });
      queryClient.invalidateQueries({ queryKey: settlementKeys.detail(data.id) });
      toast.success('Dispute resolved and marked as settled!');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to resolve dispute';
      toast.error(msg);
    },
  });
};
