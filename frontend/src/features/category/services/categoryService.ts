import apiClient from '@/core/api/client';

export interface Category {
  id: string;
  name: string;
  icon: string;
  color: string;
  systemCategory: boolean;
}

export interface CategoryRequest {
  name: string;
  icon: string;
  color: string;
}

export const categoryService = {
  getCategories: async (): Promise<Category[]> => {
    const res = await apiClient.get('/categories');
    return res.data.data;
  },

  createCategory: async (data: CategoryRequest): Promise<Category> => {
    const res = await apiClient.post('/categories', data);
    return res.data.data;
  },

  updateCategory: async (id: string, data: CategoryRequest): Promise<Category> => {
    const res = await apiClient.put(`/categories/${id}`, data);
    return res.data.data;
  },

  deleteCategory: async (id: string): Promise<void> => {
    await apiClient.delete(`/categories/${id}`);
  },
};
