import apiClient from '../../../core/api/client';
import { ApiResponse } from '../../../types/ApiResponse';
import {
  SettlementResponse,
  SettlementSummaryResponse,
  ConfirmSettlementRequest,
  DisputeSettlementRequest,
} from '../types';
import { PaginatedResponse } from '../../group/types/group';

export const settlementApi = {
  generateSettlements: async (
    groupId: string,
    tripId?: string
  ): Promise<SettlementSummaryResponse> => {
    const response = await apiClient.post<ApiResponse<SettlementSummaryResponse>>(
      `/groups/${groupId}/settlements/generate`,
      {},
      { params: { tripId } }
    );
    return response.data.data;
  },

  getSettlementSummary: async (groupId: string): Promise<SettlementSummaryResponse> => {
    const response = await apiClient.get<ApiResponse<SettlementSummaryResponse>>(
      `/groups/${groupId}/settlements`
    );
    return response.data.data;
  },

  getSettlementsByTrip: async (
    groupId: string,
    tripId: string,
    page = 0,
    size = 10
  ): Promise<PaginatedResponse<SettlementResponse>> => {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<SettlementResponse>>>(
      `/groups/${groupId}/settlements/trip/${tripId}`,
      { params: { page, size } }
    );
    return response.data.data;
  },

  getMySettlements: async (groupId: string): Promise<SettlementResponse[]> => {
    const response = await apiClient.get<ApiResponse<SettlementResponse[]>>(
      `/groups/${groupId}/settlements/me`
    );
    return response.data.data;
  },

  getSettlementById: async (id: string): Promise<SettlementResponse> => {
    const response = await apiClient.get<ApiResponse<SettlementResponse>>(`/settlements/${id}`);
    return response.data.data;
  },

  confirmPayment: async (
    id: string,
    request: ConfirmSettlementRequest
  ): Promise<SettlementResponse> => {
    const response = await apiClient.post<ApiResponse<SettlementResponse>>(
      `/settlements/${id}/confirm`,
      request
    );
    return response.data.data;
  },

  disputeSettlement: async (
    id: string,
    request: DisputeSettlementRequest
  ): Promise<SettlementResponse> => {
    const response = await apiClient.post<ApiResponse<SettlementResponse>>(
      `/settlements/${id}/dispute`,
      request
    );
    return response.data.data;
  },

  resolveSettlement: async (id: string): Promise<SettlementResponse> => {
    const response = await apiClient.post<ApiResponse<SettlementResponse>>(
      `/settlements/${id}/resolve`
    );
    return response.data.data;
  },
};
