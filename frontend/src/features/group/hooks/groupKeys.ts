export const groupKeys = {
  all: ['groups'] as const,
  lists: () => [...groupKeys.all, 'list'] as const,
  list: (search?: string, page?: number, size?: number) =>
    [...groupKeys.lists(), { search, page, size }] as const,
  details: (id: string) => [...groupKeys.all, 'details', id] as const,
  dashboards: () => [...groupKeys.all, 'dashboard'] as const,
  dashboard: (id: string) => [...groupKeys.dashboards(), id] as const,
};
