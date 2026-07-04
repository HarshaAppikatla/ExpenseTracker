import React from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate, useSearchParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ArrowLeft } from 'lucide-react';
import { useResetPassword } from '../hooks/useAuth';
import { PasswordInput } from './PasswordInput';
import { PasswordStrength } from './PasswordStrength';
import { AuthCard } from './AuthCard';

const resetPasswordSchema = z
  .object({
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(128, 'Password must not exceed 128 characters')
      .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
      .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
      .regex(/\d/, 'Password must contain at least one number')
      .regex(/[@$!%*?&#]/, 'Password must contain at least one special character (@$!%*?&#)'),
    confirmPassword: z.string().min(1, 'Confirm password is required'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

type ResetPasswordFields = z.infer<typeof resetPasswordSchema>;

export const ResetPasswordForm: React.FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const resetPasswordMutation = useResetPassword();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<ResetPasswordFields>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      password: '',
      confirmPassword: '',
    },
  });

  const passwordVal = watch('password');

  const onSubmit = (data: ResetPasswordFields) => {
    if (!token) {
      toast.error('Reset token is missing. Please request a new link.');
      return;
    }

    resetPasswordMutation.mutate(
      {
        token,
        password: data.password,
      },
      {
        onSuccess: () => {
          toast.success('Password has been reset successfully! You can now log in.');
          navigate('/login');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to reset password. Link may be expired.');
        },
      }
    );
  };

  return (
    <AuthCard>
      {!token ? (
        <div className="flex flex-col items-center text-center gap-4 py-4">
          <p className="text-sm font-semibold text-red-500">
            Invalid or missing password reset token.
          </p>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Please check your email and ensure you followed the correct link, or request a new one.
          </p>
          <button
            onClick={() => navigate('/forgot-password')}
            className="mt-4 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs rounded-xl"
          >
            Request New Link
          </button>
        </div>
      ) : (
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          {/* New Password */}
          <PasswordInput
            label="New Password"
            placeholder="••••••••"
            error={errors.password?.message}
            register={register('password')}
          />

          {/* Password Strength */}
          <PasswordStrength password={passwordVal} />

          {/* Confirm Password */}
          <PasswordInput
            label="Confirm New Password"
            placeholder="••••••••"
            error={errors.confirmPassword?.message}
            register={register('confirmPassword')}
          />

          <button
            type="submit"
            disabled={resetPasswordMutation.isPending}
            className="w-full mt-4 py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold text-sm transition-all shadow-md shadow-indigo-600/10 hover:shadow-indigo-600/20 active:scale-[0.98] flex items-center justify-center gap-2"
          >
            {resetPasswordMutation.isPending ? 'Resetting Password...' : 'Reset Password'}
          </button>
        </form>
      )}

      <div className="text-center text-xs border-t border-slate-100 dark:border-slate-800 pt-4">
        <button
          onClick={() => navigate('/login')}
          className="flex items-center justify-center gap-2 text-slate-500 hover:text-indigo-600 dark:text-slate-400 dark:hover:text-indigo-400 transition-colors font-medium mx-auto"
        >
          <ArrowLeft size={14} />
          Back to login
        </button>
      </div>
    </AuthCard>
  );
};
