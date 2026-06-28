import apiClient from '@/core/api/client';
import { ApiResponse } from '@/types/ApiResponse';
import { useQuery } from '@tanstack/react-query';

export interface HealthDetails {
  name: string;
  version: string;
  environment: string;
  timestamp: string;
  status: string;
}

export interface InfoDetails {
  application: string;
  version: string;
  environment: string;
  javaVersion: string;
  springBootVersion: string;
  buildTime: string;
  uptime: string;
}

export const checkHealth = async (): Promise<ApiResponse<HealthDetails>> => {
  const response = await apiClient.get<ApiResponse<HealthDetails>>('/health');
  return response.data;
};

export const checkInfo = async (): Promise<ApiResponse<InfoDetails>> => {
  const response = await apiClient.get<ApiResponse<InfoDetails>>('/info');
  return response.data;
};

export const useHealthCheck = (options?: { refetchInterval?: number }) => {
  return useQuery<ApiResponse<HealthDetails>, Error>({
    queryKey: ['health'],
    queryFn: checkHealth,
    refetchInterval: options?.refetchInterval,
  });
};

export const useInfoCheck = () => {
  return useQuery<ApiResponse<InfoDetails>, Error>({
    queryKey: ['info'],
    queryFn: checkInfo,
  });
};
