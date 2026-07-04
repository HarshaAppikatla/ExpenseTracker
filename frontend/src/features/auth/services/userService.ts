import apiClient from '@/core/api/client';
import { ApiResponse } from '@/types/ApiResponse';
import { UserDto } from '../types';

export const userService = {
  getMe: async (): Promise<UserDto> => {
    const response = await apiClient.get<ApiResponse<UserDto>>('/users/me');
    return response.data.data;
  }
};
