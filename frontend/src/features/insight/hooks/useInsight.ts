import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { insightService, InsightDashboard } from '../services/insightService';

export const useInsights = (): UseQueryResult<InsightDashboard, Error> => {
  return useQuery({
    queryKey: ['insights'],
    queryFn: () => insightService.getInsights(),
    staleTime: 30000, // 30 seconds stale time matching cache TTL
  });
};
