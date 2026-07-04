import apiClient from '../../../core/api/client';
import { ApiResponse } from '../../../types/ApiResponse';
import {
  GroupDto,
  CreateGroupRequest,
  UpdateGroupRequest,
  JoinGroupRequest,
  TransferOwnershipRequest,
  UpdateMemberRoleRequest,
} from '../types/group';

export const groupCommands = {
  createGroup: async (request: CreateGroupRequest): Promise<GroupDto> => {
    const response = await apiClient.post<ApiResponse<GroupDto>>('/groups', request);
    return response.data.data;
  },

  updateGroup: async (id: string, request: UpdateGroupRequest): Promise<GroupDto> => {
    const response = await apiClient.patch<ApiResponse<GroupDto>>(`/groups/${id}`, request);
    return response.data.data;
  },

  archiveGroup: async (id: string): Promise<GroupDto> => {
    const response = await apiClient.post<ApiResponse<GroupDto>>(`/groups/${id}/archive`);
    return response.data.data;
  },

  restoreGroup: async (id: string): Promise<GroupDto> => {
    const response = await apiClient.post<ApiResponse<GroupDto>>(`/groups/${id}/restore`);
    return response.data.data;
  },

  deleteGroup: async (id: string): Promise<void> => {
    const response = await apiClient.delete<ApiResponse<void>>(`/groups/${id}`);
    return response.data.data;
  },

  joinGroup: async (request: JoinGroupRequest): Promise<GroupDto> => {
    const response = await apiClient.post<ApiResponse<GroupDto>>('/groups/join', request);
    return response.data.data;
  },

  leaveGroup: async (id: string): Promise<void> => {
    const response = await apiClient.post<ApiResponse<void>>(`/groups/${id}/leave`);
    return response.data.data;
  },

  removeMember: async (groupId: string, memberId: string): Promise<void> => {
    const response = await apiClient.delete<ApiResponse<void>>(`/groups/${groupId}/members/${memberId}`);
    return response.data.data;
  },

  updateMemberRole: async (
    groupId: string,
    memberId: string,
    request: UpdateMemberRoleRequest
  ): Promise<void> => {
    const response = await apiClient.patch<ApiResponse<void>>(
      `/groups/${groupId}/members/${memberId}/role`,
      request
    );
    return response.data.data;
  },

  transferOwnership: async (
    groupId: string,
    request: TransferOwnershipRequest
  ): Promise<void> => {
    const response = await apiClient.post<ApiResponse<void>>(
      `/groups/${groupId}/transfer-ownership`,
      request
    );
    return response.data.data;
  },
};
