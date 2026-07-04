import apiClient from '@/core/api/client';

export interface UserProfile {
  id: string;
  userId: string;
  preferredCurrency: string;
  openingBalance: number;
  onboardingCompleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OnboardingRequest {
  preferredCurrency: string;
  openingBalance: number;
  initialMonthlyIncome?: number | null;
}

export const profileService = {
  getProfile: async (): Promise<UserProfile> => {
    const res = await apiClient.get('/profile');
    return res.data.data;
  },

  onboard: async (data: OnboardingRequest): Promise<UserProfile> => {
    const res = await apiClient.post('/profile/onboarding', data);
    return res.data.data;
  },

  updateProfile: async (data: OnboardingRequest): Promise<UserProfile> => {
    const res = await apiClient.put('/profile', data);
    return res.data.data;
  },
};
