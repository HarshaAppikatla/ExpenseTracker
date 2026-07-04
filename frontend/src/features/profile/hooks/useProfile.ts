import { useQuery, useMutation, useQueryClient, UseQueryResult, UseMutationResult } from '@tanstack/react-query';
import { profileService, UserProfile, OnboardingRequest } from '../services/profileService';

export const useProfile = (enabled = true): UseQueryResult<UserProfile, Error> => {
  return useQuery({
    queryKey: ['profile'],
    queryFn: () => profileService.getProfile(),
    retry: false, // Don't retry since 404/400 means onboarding is required
    enabled,
  });
};

export const useOnboard = (): UseMutationResult<UserProfile, Error, OnboardingRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: OnboardingRequest) => profileService.onboard(data),
    onSuccess: (data) => {
      queryClient.setQueryData(['profile'], data);
    },
  });
};

export const useUpdateProfile = (): UseMutationResult<UserProfile, Error, OnboardingRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: OnboardingRequest) => profileService.updateProfile(data),
    onSuccess: (data) => {
      queryClient.setQueryData(['profile'], data);
    },
  });
};
