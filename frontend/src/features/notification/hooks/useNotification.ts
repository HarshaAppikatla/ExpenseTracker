import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import toast from 'react-hot-toast';
import { notificationService } from '../services/notificationService';
import { useAuthContext } from '@/hooks/useAuthContext';

export const useNotifications = (page = 0, size = 20) =>
  useQuery({
    queryKey: ['notifications', page, size],
    queryFn: () => notificationService.getNotifications(page, size),
    staleTime: 10_000,
  });

export const useLatestNotifications = (limit = 5) =>
  useQuery({
    queryKey: ['latest-notifications', limit],
    queryFn: () => notificationService.getLatestNotifications(limit),
    staleTime: 10_000,
  });

export const useUnreadCount = () =>
  useQuery({
    queryKey: ['notifications-unread-count'],
    queryFn: () => notificationService.getUnreadCount(),
    refetchInterval: 60_000, // Fallback poll every 60s
  });

export const useMarkAsRead = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => notificationService.markAsRead(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] });
      qc.invalidateQueries({ queryKey: ['notifications-unread-count'] });
      qc.invalidateQueries({ queryKey: ['latest-notifications'] });
    },
  });
};

export const useMarkAllAsRead = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => notificationService.markAllAsRead(),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] });
      qc.invalidateQueries({ queryKey: ['notifications-unread-count'] });
      qc.invalidateQueries({ queryKey: ['latest-notifications'] });
    },
  });
};

export const useArchiveNotification = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => notificationService.archive(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] });
      qc.invalidateQueries({ queryKey: ['notifications-unread-count'] });
      qc.invalidateQueries({ queryKey: ['latest-notifications'] });
    },
  });
};

// SSE stream hook for real-time notifications
export const useSseConnection = () => {
  const qc = useQueryClient();
  const { isAuthenticated } = useAuthContext();

  useEffect(() => {
    if (!isAuthenticated) return;

    const apiBaseUrl = (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
    const streamUrl = `${apiBaseUrl}/notifications/stream`;

    console.debug('Establishing SSE connection to:', streamUrl);
    const eventSource = new EventSource(streamUrl, { withCredentials: true });

    eventSource.addEventListener('connect', (event: MessageEvent) => {
      console.debug('SSE connection handshake successful:', event.data);
    });

    eventSource.addEventListener('notification', (event: MessageEvent) => {
      try {
        const payload = JSON.parse(event.data);
        console.debug('Received SSE notification:', payload);

        // Invalidate React Query cache
        qc.invalidateQueries({ queryKey: ['notifications'] });
        qc.invalidateQueries({ queryKey: ['notifications-unread-count'] });
        qc.invalidateQueries({ queryKey: ['latest-notifications'] });

        // Trigger dynamic toast
        toast.success(payload.title || 'New alert received', {
          description: payload.message,
          duration: 6000,
          position: 'top-right',
        } as any);
      } catch (err) {
        console.error('Failed to parse SSE payload:', err);
      }
    });

    eventSource.addEventListener('heartbeat', () => {
      // Trace-level heartbeat logging to keep connection active
    });

    eventSource.onerror = (err) => {
      console.warn('SSE stream disconnected or encountered error. Reconnecting...', err);
    };

    return () => {
      console.debug('Closing SSE connection...');
      eventSource.close();
    };
  }, [isAuthenticated, qc]);
};
