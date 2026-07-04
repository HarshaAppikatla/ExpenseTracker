import { useMutation, UseMutationResult } from '@tanstack/react-query';
import { authService } from '../services/authService';
import { LoginResponse, UserDto } from '../types';

export const useLogin = (): UseMutationResult<LoginResponse, Error, any> => {
  return useMutation({
    mutationFn: (data: any) => authService.login(data),
  });
};

export const useRegister = (): UseMutationResult<UserDto, Error, any> => {
  return useMutation({
    mutationFn: (data: any) => authService.register(data),
  });
};

export const useForgotPassword = (): UseMutationResult<void, Error, string> => {
  return useMutation({
    mutationFn: (email: string) => authService.forgotPassword(email),
  });
};

export const useResetPassword = (): UseMutationResult<void, Error, any> => {
  return useMutation({
    mutationFn: (data: any) => authService.resetPassword(data),
  });
};

export const useVerifyEmail = (): UseMutationResult<void, Error, string> => {
  return useMutation({
    mutationFn: (token: string) => authService.verifyEmail(token),
  });
};

export const useResendVerification = (): UseMutationResult<void, Error, string> => {
  return useMutation({
    mutationFn: (email: string) => authService.resendVerification(email),
  });
};
