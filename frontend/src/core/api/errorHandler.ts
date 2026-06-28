import axios, { AxiosError } from 'axios';

export interface StandardError {
  message: string;
  statusCode?: number;
  fields?: Record<string, string>;
  raw?: unknown;
}

export const handleApiError = (error: unknown): StandardError => {
  const standardError: StandardError = {
    message: 'An unexpected connection issue occurred. Please check your network and retry.',
  };

  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ success?: boolean; message?: string; data?: unknown }>;
    standardError.raw = axiosError;

    if (axiosError.response) {
      standardError.statusCode = axiosError.response.status;
      const responseData = axiosError.response.data;

      if (responseData && responseData.message) {
        standardError.message = responseData.message;
      } else {
        standardError.message = `Server responded with error status ${axiosError.response.status}`;
      }

      if (responseData && typeof responseData.data === 'object' && responseData.data !== null) {
        standardError.fields = responseData.data as Record<string, string>;
      }
    } else if (axiosError.request) {
      standardError.message = 'Unable to establish a connection with the server. Please verify the backend is online.';
    }
  } else if (error instanceof Error) {
    standardError.message = error.message;
  }

  return standardError;
};
