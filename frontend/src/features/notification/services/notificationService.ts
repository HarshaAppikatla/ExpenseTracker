import apiClient from '@/core/api/client';

export interface Notification {
  id: string;
  userId: string;
  notificationType: string;
  title: string;
  message: string;
  status: 'UNREAD' | 'READ' | 'ARCHIVED';
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';
  createdAt: string;
}

export const notificationService = {
  getAll: async (): Promise<Notification[]> => {
    const res = await apiClient.get('/notifications');
    return res.data.data;
  },

  getUnreadCount: async (): Promise<number> => {
    const res = await apiClient.get('/notifications/unread-count');
    return res.data.data;
  },

  markAsRead: async (id: string): Promise<Notification> => {
    const res = await apiClient.put(`/notifications/${id}/read`);
    return res.data.data;
  },

  archive: async (id: string): Promise<void> => {
    await apiClient.delete(`/notifications/${id}`);
  },
};
