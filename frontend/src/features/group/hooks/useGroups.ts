import { useQuery } from '@tanstack/react-query';
import { groupQueries } from '../api';
import { groupKeys } from './groupKeys';

export const useMyGroups = (search?: string, page = 0, size = 10) => {
  return useQuery({
    queryKey: groupKeys.list(search, page, size),
    queryFn: () => groupQueries.getMyGroups(search, page, size),
  });
};
