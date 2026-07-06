export const tripKeys = {
  all: ['trips'] as const,
  lists: (groupId: string) => [...tripKeys.all, 'list', groupId] as const,
  list: (groupId: string, page?: number, size?: number) =>
    [...tripKeys.lists(groupId), { page, size }] as const,
  details: (id: string) => [...tripKeys.all, 'details', id] as const,
  timelines: (id: string) => [...tripKeys.all, 'timeline', id] as const,
  timeline: (id: string, page?: number, size?: number) =>
    [...tripKeys.timelines(id), { page, size }] as const,
};
export default tripKeys;
