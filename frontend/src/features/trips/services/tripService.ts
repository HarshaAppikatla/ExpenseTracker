import apiClient from '../../../core/api/client';
import { ApiResponse } from '../../../types/ApiResponse';
import {
  TripDto,
  CreateTripRequest,
  UpdateTripRequest,
  TripActivityDto,
  PaginatedResponse
} from '../types/trip';

export const tripService = {
  createTrip: async (request: CreateTripRequest): Promise<TripDto> => {
    const response = await apiClient.post<ApiResponse<TripDto>>('/trips', request);
    return response.data.data;
  },

  updateTrip: async (id: string, request: UpdateTripRequest): Promise<TripDto> => {
    const response = await apiClient.patch<ApiResponse<TripDto>>(`/trips/${id}`, request);
    return response.data.data;
  },

  updateTripStatus: async (id: string, status: string): Promise<TripDto> => {
    const response = await apiClient.patch<ApiResponse<TripDto>>(`/trips/${id}/status`, { status });
    return response.data.data;
  },

  deleteTrip: async (id: string): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/trips/${id}`);
  },

  getTripDetails: async (id: string): Promise<TripDto> => {
    const response = await apiClient.get<ApiResponse<TripDto>>(`/trips/${id}`);
    return response.data.data;
  },

  getGroupTrips: async (
    groupId: string,
    page = 0,
    size = 10
  ): Promise<PaginatedResponse<TripDto>> => {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<TripDto>>>(
      `/groups/${groupId}/trips`,
      { params: { page, size } }
    );
    return response.data.data;
  },

  inviteParticipant: async (tripId: string, userId: string): Promise<TripDto> => {
    const response = await apiClient.post<ApiResponse<TripDto>>(`/trips/${tripId}/participants/invite`, { userId });
    return response.data.data;
  },

  acceptInvite: async (tripId: string): Promise<TripDto> => {
    const response = await apiClient.post<ApiResponse<TripDto>>(`/trips/${tripId}/participants/accept`);
    return response.data.data;
  },

  declineInvite: async (tripId: string): Promise<TripDto> => {
    const response = await apiClient.post<ApiResponse<TripDto>>(`/trips/${tripId}/participants/decline`);
    return response.data.data;
  },

  leaveTrip: async (tripId: string): Promise<TripDto> => {
    const response = await apiClient.post<ApiResponse<TripDto>>(`/trips/${tripId}/participants/leave`);
    return response.data.data;
  },

  removeParticipant: async (tripId: string, userId: string): Promise<TripDto> => {
    const response = await apiClient.delete<ApiResponse<TripDto>>(`/trips/${tripId}/participants/${userId}`);
    return response.data.data;
  },

  getTripTimeline: async (
    tripId: string,
    page = 0,
    size = 20
  ): Promise<PaginatedResponse<TripActivityDto>> => {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<TripActivityDto>>>(
      `/trips/${tripId}/timeline`,
      { params: { page, size } }
    );
    return response.data.data;
  },
};
export default tripService;
