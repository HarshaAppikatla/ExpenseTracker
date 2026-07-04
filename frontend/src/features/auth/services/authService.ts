import apiClient from '@/core/api/client';
import { ApiResponse } from '@/types/ApiResponse';
import { LoginResponse, UserDto } from '../types';

export const authService = {
  register: async (data: any): Promise<UserDto> => {
    const response = await apiClient.post<ApiResponse<UserDto>>('/auth/register', data);
    return response.data.data;
  },

  checkEmail: async (email: string): Promise<boolean> => {
    const response = await apiClient.post<ApiResponse<boolean>>('/auth/check-email', { email });
    return response.data.data;
  },

  verifyEmail: async (token: string): Promise<void> => {
    await apiClient.post<ApiResponse<void>>(`/auth/verify-email?token=${encodeURIComponent(token)}`);
  },

  resendVerification: async (email: string): Promise<void> => {
    await apiClient.post<ApiResponse<void>>('/auth/resend-verification', { email });
  },

  login: async (data: any): Promise<LoginResponse> => {
    const response = await apiClient.post<ApiResponse<LoginResponse>>('/auth/login', data);
    return response.data.data;
  },

  logout: async (refreshToken: string): Promise<void> => {
    await apiClient.post<ApiResponse<void>>('/auth/logout', { refreshToken });
  },

  forgotPassword: async (email: string): Promise<void> => {
    await apiClient.post<ApiResponse<void>>('/auth/forgot-password', { email });
  },

  resetPassword: async (data: any): Promise<void> => {
    await apiClient.post<ApiResponse<void>>('/auth/reset-password', data);
  }
};
