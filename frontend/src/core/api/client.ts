import axios from 'axios';
import { API_TIMEOUT } from '../constants';
import { setupInterceptors } from './interceptors';

const apiClient = axios.create({
  baseURL: (((import.meta as any).env?.VITE_API_BASE_URL) as string) || 'http://localhost:8080/api/v1',
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

setupInterceptors(apiClient);

export default apiClient;
