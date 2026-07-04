import { useQuery } from '@tanstack/react-query';
import { groupQueries } from '../api';
import { groupKeys } from './groupKeys';

export const useGroupDetails = (id: string, enabled = true) => {
  return useQuery({
    queryKey: groupKeys.details(id),
    queryFn: () => groupQueries.getGroupDetails(id),
    enabled: !!id && enabled,
  });
};
