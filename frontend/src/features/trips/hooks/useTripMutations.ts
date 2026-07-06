import { useMutation, useQueryClient } from '@tanstack/react-query';
import tripService from '../services/tripService';
import tripKeys from './tripKeys';
import toast from 'react-hot-toast';
import { UpdateTripRequest } from '../types/trip';

/** Extracts a human-readable message from an API error (StandardError or AxiosError). */
const getErrMsg = (err: any, fallback: string): string =>
  err?.message || err?.response?.data?.message || fallback;

export const useCreateTrip = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: tripService.createTrip,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: tripKeys.lists(data.groupId) });
      toast.success('Trip created successfully!');
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to create trip'));
    },
  });
};

export const useUpdateTrip = (tripId: string, groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: UpdateTripRequest) => tripService.updateTrip(tripId, request),
    onSuccess: (data) => {
      queryClient.setQueryData(tripKeys.details(tripId), data);
      queryClient.invalidateQueries({ queryKey: tripKeys.details(tripId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.lists(groupId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.timelines(tripId) });
      toast.success('Trip updated successfully!');
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to update trip'));
    },
  });
};

export const useUpdateTripStatus = (tripId: string, groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (status: string) => tripService.updateTripStatus(tripId, status),
    onSuccess: (data) => {
      queryClient.setQueryData(tripKeys.details(tripId), data);
      queryClient.invalidateQueries({ queryKey: tripKeys.details(tripId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.lists(groupId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.timelines(tripId) });
      toast.success(`Trip status changed to ${data.status}!`);
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to change trip status'));
    },
  });
};

export const useDeleteTrip = (groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: tripService.deleteTrip,
    onSuccess: (_, tripId) => {
      queryClient.invalidateQueries({ queryKey: tripKeys.lists(groupId) });
      queryClient.removeQueries({ queryKey: tripKeys.details(tripId) });
      queryClient.removeQueries({ queryKey: tripKeys.timelines(tripId) });
      toast.success('Trip deleted successfully!');
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to delete trip'));
    },
  });
};

export const useInviteParticipant = (tripId: string, _groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userId: string) => tripService.inviteParticipant(tripId, userId),
    onSuccess: (data) => {
      queryClient.setQueryData(tripKeys.details(tripId), data);
      queryClient.invalidateQueries({ queryKey: tripKeys.details(tripId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.timelines(tripId) });
      toast.success('Participant invited successfully!');
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to invite participant'));
    },
  });
};

export const useAcceptInvite = (tripId: string, _groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => tripService.acceptInvite(tripId),
    onSuccess: (data) => {
      queryClient.setQueryData(tripKeys.details(tripId), data);
      queryClient.invalidateQueries({ queryKey: tripKeys.details(tripId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.timelines(tripId) });
      toast.success('Joined trip successfully!');
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to join trip'));
    },
  });
};

export const useDeclineInvite = (tripId: string, _groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => tripService.declineInvite(tripId),
    onSuccess: (data) => {
      queryClient.setQueryData(tripKeys.details(tripId), data);
      queryClient.invalidateQueries({ queryKey: tripKeys.details(tripId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.timelines(tripId) });
      toast.success('Declined invitation successfully.');
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to decline invitation'));
    },
  });
};

export const useLeaveTrip = (tripId: string, _groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => tripService.leaveTrip(tripId),
    onSuccess: (data) => {
      queryClient.setQueryData(tripKeys.details(tripId), data);
      queryClient.invalidateQueries({ queryKey: tripKeys.details(tripId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.timelines(tripId) });
      toast.success('Left trip successfully.');
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to leave trip'));
    },
  });
};

export const useRemoveParticipant = (tripId: string, _groupId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userId: string) => tripService.removeParticipant(tripId, userId),
    onSuccess: (data) => {
      queryClient.setQueryData(tripKeys.details(tripId), data);
      queryClient.invalidateQueries({ queryKey: tripKeys.details(tripId) });
      queryClient.invalidateQueries({ queryKey: tripKeys.timelines(tripId) });
      toast.success('Participant removed successfully.');
    },
    onError: (err: any) => {
      toast.error(getErrMsg(err, 'Failed to remove participant'));
    },
  });
};
