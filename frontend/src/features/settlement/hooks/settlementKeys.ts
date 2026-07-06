export const settlementKeys = {
  all: ['settlements'] as const,
  summaries: () => [...settlementKeys.all, 'summary'] as const,
  summary: (groupId: string) => [...settlementKeys.summaries(), groupId] as const,
  mySettlements: (groupId: string) => [...settlementKeys.all, 'me', groupId] as const,
  trips: () => [...settlementKeys.all, 'trip'] as const,
  tripList: (groupId: string, tripId: string, page: number) => [...settlementKeys.trips(), groupId, tripId, page] as const,
  details: () => [...settlementKeys.all, 'detail'] as const,
  detail: (id: string) => [...settlementKeys.details(), id] as const,
};
