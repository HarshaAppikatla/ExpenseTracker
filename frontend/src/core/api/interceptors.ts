import { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { handleApiError } from './errorHandler';

export const setupInterceptors = (axiosInstance: AxiosInstance): void => {
  axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      // Future authentication token injection can happen here
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => {
      return response;
    },
    (error) => {
      return Promise.reject(handleApiError(error));
    }
  );
};
