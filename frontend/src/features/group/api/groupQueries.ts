import apiClient from '../../../core/api/client';
import { ApiResponse } from '../../../types/ApiResponse';
import { GroupDto, GroupDashboardDto, PaginatedResponse } from '../types/group';

export const groupQueries = {
  getMyGroups: async (
    search?: string,
    page = 0,
    size = 10
  ): Promise<PaginatedResponse<GroupDto>> => {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<GroupDto>>>('/groups', {
      params: { search, page, size },
    });
    return response.data.data;
  },

  getGroupDetails: async (id: string): Promise<GroupDto> => {
    const response = await apiClient.get<ApiResponse<GroupDto>>(`/groups/${id}`);
    return response.data.data;
  },

  getGroupDashboard: async (id: string): Promise<GroupDashboardDto> => {
    const response = await apiClient.get<ApiResponse<GroupDashboardDto>>(`/groups/${id}/dashboard`);
    return response.data.data;
  },
};
