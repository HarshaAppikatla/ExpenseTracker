import apiClient from '@/core/api/client';
import { Notification, PageResponse } from '../types';

export const notificationService = {
  getNotifications: async (page = 0, size = 20): Promise<PageResponse<Notification>> => {
    const res = await apiClient.get('/notifications', {
      params: { page, size }
    });
    return res.data.data;
  },

  getLatestNotifications: async (limit = 5): Promise<Notification[]> => {
    const res = await apiClient.get('/notifications/latest', {
      params: { limit }
    });
    return res.data.data;
  },

  getUnreadCount: async (): Promise<number> => {
    const res = await apiClient.get('/notifications/unread-count');
    return res.data.data.unreadCount;
  },

  markAsRead: async (id: string): Promise<Notification> => {
    const res = await apiClient.put(`/notifications/${id}/read`);
    return res.data.data;
  },

  markAllAsRead: async (): Promise<void> => {
    await apiClient.put('/notifications/read-all');
  },

  archive: async (id: string): Promise<void> => {
    await apiClient.delete(`/notifications/${id}`);
  },
};
