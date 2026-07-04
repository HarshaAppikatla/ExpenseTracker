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

      // Handle rate limiting with a friendly message
      if (axiosError.response.status === 429) {
        standardError.message =
          (responseData && responseData.message) ||
          'Too many attempts. Please wait a moment before trying again.';
        return standardError;
      }

      // Handle gateway/server-down errors
      if ([502, 503, 504].includes(axiosError.response.status)) {
        standardError.message = 'The server is temporarily unavailable. Please try again shortly.';
        return standardError;
      }

      if (responseData && responseData.message) {
        standardError.message = responseData.message;
      } else {
        standardError.message = `Server responded with error status ${axiosError.response.status}`;
      }

      if (responseData && typeof responseData.data === 'object' && responseData.data !== null) {
        standardError.fields = responseData.data as Record<string, string>;
      }
    } else if (axiosError.request) {
      // Request was made but no response received — true network/connection failure
      standardError.message = 'Unable to establish a connection with the server. Please verify the backend is online.';
    }
  } else if (error instanceof Error) {
    standardError.message = error.message;
  }

  return standardError;
};
