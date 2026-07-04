import apiClient from '@/core/api/client';

export interface Receipt {
  id: string;
  expenseId: string;
  originalFileName: string;
  fileSize: number;
  mimeType: string;
  uploadedAt: string;
}

export const receiptService = {
  uploadReceipt: async (expenseId: string, file: File): Promise<Receipt> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await apiClient.post(`/receipts`, formData, {
      params: { expenseId },
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data.data;
  },

  replaceReceipt: async (receiptId: string, expenseId: string, file: File): Promise<Receipt> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await apiClient.put(`/receipts/${receiptId}`, formData, {
      params: { expenseId },
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data.data;
  },

  deleteReceipt: async (receiptId: string): Promise<void> => {
    await apiClient.delete(`/receipts/${receiptId}`);
  },

  getDownloadUrl: (receiptId: string): string => {
    const baseUrl = apiClient.defaults.baseURL || 'http://localhost:8080/api/v1';
    return `${baseUrl}/receipts/${receiptId}`;
  },
};
