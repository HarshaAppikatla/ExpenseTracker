import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { handleApiError } from './errorHandler';
import { tokenService } from '@/features/auth/services/tokenService';

interface FailedRequest {
  resolve: (token: string) => void;
  reject: (error: any) => void;
}

let isRefreshing = false;
let failedQueue: FailedRequest[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
};

export const setupInterceptors = (axiosInstance: AxiosInstance): void => {
  axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      // Trigger idle timer reset
      window.dispatchEvent(new Event('api-activity'));
      const token = tokenService.getAccessToken();
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      window.dispatchEvent(new Event('api-activity'));
      return Promise.reject(error);
    }
  );

  axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => {
      window.dispatchEvent(new Event('api-activity'));
      return response;
    },
    async (error) => {
      window.dispatchEvent(new Event('api-activity'));
      const originalRequest = error.config;

      if (!originalRequest) {
        return Promise.reject(handleApiError(error));
      }

      // Check for 401 Unauthorized and that the request has not been retried yet
      if (error.response?.status === 401 && !originalRequest._retry) {
        // Prevent infinite loops on auth endpoints
        if (
          originalRequest.url?.includes('/auth/login') ||
          originalRequest.url?.includes('/auth/refresh') ||
          originalRequest.url?.includes('/auth/register')
        ) {
          return Promise.reject(handleApiError(error));
        }

        if (isRefreshing) {
          return new Promise<string>((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          })
            .then((token) => {
              originalRequest.headers.Authorization = `Bearer ${token}`;
              return axiosInstance(originalRequest);
            })
            .catch((err) => {
              return Promise.reject(err);
            });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          const refreshToken = tokenService.getRefreshToken();
          if (!refreshToken) {
            throw new Error('No session active');
          }

          const baseUrl = axiosInstance.defaults.baseURL || 'http://localhost:8080/api/v1';
          const refreshRes = await axios.post(`${baseUrl}/auth/refresh`, {
            refreshToken,
          });

          const { accessToken, refreshToken: newRefreshToken } = refreshRes.data.data;
          tokenService.setAccessToken(accessToken);
          tokenService.setRefreshToken(newRefreshToken);

          processQueue(null, accessToken);
          isRefreshing = false;

          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return axiosInstance(originalRequest);
        } catch (refreshError) {
          processQueue(refreshError, null);
          isRefreshing = false;
          tokenService.removeTokens();

          // Redirect to login page if user was in a private space
          if (
            window.location.pathname !== '/login' &&
            window.location.pathname !== '/register' &&
            window.location.pathname !== '/'
          ) {
            window.location.href = `/login?expired=true`;
          }
          return Promise.reject(handleApiError(refreshError));
        }
      }

      return Promise.reject(handleApiError(error));
    }
  );
};
