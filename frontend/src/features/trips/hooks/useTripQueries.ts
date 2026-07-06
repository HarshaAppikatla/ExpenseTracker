import { useQuery } from '@tanstack/react-query';
import tripService from '../services/tripService';
import tripKeys from './tripKeys';

export const useGroupTrips = (groupId: string, page = 0, size = 10) => {
  return useQuery({
    queryKey: tripKeys.list(groupId, page, size),
    queryFn: () => tripService.getGroupTrips(groupId, page, size),
    enabled: !!groupId,
  });
};

export const useTripDetails = (tripId: string) => {
  return useQuery({
    queryKey: tripKeys.details(tripId),
    queryFn: () => tripService.getTripDetails(tripId),
    enabled: !!tripId,
  });
};

export const useTripTimeline = (tripId: string, page = 0, size = 20) => {
  return useQuery({
    queryKey: tripKeys.timeline(tripId, page, size),
    queryFn: () => tripService.getTripTimeline(tripId, page, size),
    enabled: !!tripId,
  });
};
