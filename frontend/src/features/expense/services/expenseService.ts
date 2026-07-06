import apiClient from '@/core/api/client';
import { Category } from '@/features/category/services/categoryService';

// Legacy Personal Expense Types
export interface Expense {
  id: string;
  userId: string;
  groupId?: string | null;
  category: Category;
  amount: number;
  currencyCode: string;
  expenseDate: string;
  merchant?: string;
  description?: string;
  notes?: string;
  latitude?: number;
  longitude?: number;
  locationName?: string;
  address?: string;
  status: string;
  tags: string[];
  receipt?: any;
  createdAt: string;
  updatedAt: string;
}

export interface ExpenseRequest {
  categoryId: string;
  amount: number;
  currencyCode?: string;
  expenseDate: string;
  merchant?: string;
  description?: string;
  notes?: string;
  latitude?: number | null;
  longitude?: number | null;
  locationName?: string;
  address?: string;
  tags?: string[];
}

// New Group / Shared Expense Types
export type GroupExpenseCategory = 'FOOD' | 'LODGING' | 'TRANSPORT' | 'ENTERTAINMENT' | 'SHOPPING' | 'OTHER';
export type SplitType = 'EQUAL' | 'EXACT' | 'PERCENTAGE' | 'SHARES';
export type GroupExpenseStatus = 'DRAFT' | 'POSTED' | 'VOID';
export type StorageProvider = 'LOCAL' | 'S3' | 'SUPABASE' | 'CLOUDINARY';

export interface ExpenseParticipant {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
}

export interface ExpenseSplit {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  owedAmount: number;
  allocationValue?: number;
}

export interface ExpenseAttachment {
  id: string;
  url: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  storageProvider: StorageProvider;
}

export interface GroupExpense {
  id: string;
  groupId: string;
  tripId?: string | null;
  description: string;
  category: GroupExpenseCategory;
  categoryType: 'SYSTEM' | 'CUSTOM';
  amount: number;
  currency: string;
  paidByUserId: string;
  paidByUserName: string;
  createdByUserId: string;
  createdByUserName: string;
  status: GroupExpenseStatus;
  splitType: SplitType;
  expenseDate: string;
  participants: ExpenseParticipant[];
  splits: ExpenseSplit[];
  attachments: ExpenseAttachment[];
  version: number;
}

export interface CreateGroupExpenseRequest {
  tripId?: string | null;
  description: string;
  category: GroupExpenseCategory;
  amount: number;
  currency: string;
  paidByUserId: string;
  expenseDate: string;
  splitType: SplitType;
  allocationValues: Record<string, number>;
}

export interface UpdateGroupExpenseRequest {
  description: string;
  category: GroupExpenseCategory;
  amount: number;
  currency: string;
  paidByUserId: string;
  expenseDate: string;
  splitType: SplitType;
  allocationValues: Record<string, number>;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export const expenseService = {
  // Read-only User Expense Ledger Method
  getExpenses: async (page = 0, size = 20, sort = 'expenseDate,desc'): Promise<PaginatedResponse<GroupExpense>> => {
    const res = await apiClient.get('/expenses', {
      params: { page, size, sort },
    });
    return res.data.data;
  },

  getExpenseById: async (id: string): Promise<Expense> => {
    const res = await apiClient.get(`/expenses/${id}`);
    return res.data.data;
  },

  createExpense: async (data: ExpenseRequest): Promise<Expense> => {
    const res = await apiClient.post('/expenses', data);
    return res.data.data;
  },

  updateExpense: async (id: string, data: ExpenseRequest): Promise<Expense> => {
    const res = await apiClient.put(`/expenses/${id}`, data);
    return res.data.data;
  },

  deleteExpense: async (id: string): Promise<void> => {
    await apiClient.delete(`/expenses/${id}`);
  },

  getMerchantSuggestions: async (query: string, limit = 10): Promise<string[]> => {
    if (!query.trim()) return [];
    const res = await apiClient.get('/expenses/merchants/suggestions', {
      params: { query, limit },
    });
    return res.data.data;
  },

  // New Group / Shared Expense Methods
  getGroupExpenses: async (
    groupId: string,
    tripId?: string | null,
    page = 0,
    size = 20,
    sort = 'expenseDate,desc'
  ): Promise<PaginatedResponse<GroupExpense>> => {
    const params: Record<string, any> = { page, size, sort };
    if (tripId) {
      params.tripId = tripId;
    }
    const res = await apiClient.get(`/groups/${groupId}/expenses`, { params });
    return res.data.data;
  },

  getGroupExpenseById: async (id: string): Promise<GroupExpense> => {
    const res = await apiClient.get(`/expenses/${id}`);
    return res.data.data;
  },

  createGroupExpense: async (groupId: string, data: CreateGroupExpenseRequest): Promise<GroupExpense> => {
    const res = await apiClient.post(`/groups/${groupId}/expenses`, data);
    return res.data.data;
  },

  updateGroupExpense: async (id: string, data: UpdateGroupExpenseRequest): Promise<GroupExpense> => {
    const res = await apiClient.patch(`/expenses/${id}`, data);
    return res.data.data;
  },

  deleteGroupExpense: async (id: string): Promise<void> => {
    await apiClient.delete(`/expenses/${id}`);
  },

  transitionStatus: async (id: string, status: GroupExpenseStatus): Promise<GroupExpense> => {
    const res = await apiClient.post(`/expenses/${id}/status`, { status });
    return res.data.data;
  },

  addAttachment: async (
    expenseId: string,
    params: {
      url: string;
      fileName: string;
      fileSize: number;
      fileType: string;
      storageProvider: StorageProvider;
    }
  ): Promise<GroupExpense> => {
    const res = await apiClient.post(`/expenses/${expenseId}/attachments`, null, {
      params,
    });
    return res.data.data;
  },
};
