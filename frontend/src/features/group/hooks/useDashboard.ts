import { useQuery } from '@tanstack/react-query';
import { groupQueries } from '../api/groupQueries';
import { groupKeys } from './groupKeys';

export const useGroupDashboard = (id: string, enabled = true) => {
  return useQuery({
    queryKey: groupKeys.dashboard(id),
    queryFn: () => groupQueries.getGroupDashboard(id),
    enabled: !!id && enabled,
  });
};
