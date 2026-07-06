import React from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useRegister } from '../hooks/useAuth';
import { PasswordInput } from './PasswordInput';
import { PasswordStrength } from './PasswordStrength';
import { AuthCard } from './AuthCard';

const registerSchema = z
  .object({
    fullName: z
      .string()
      .min(1, 'Full name is required')
      .max(100, 'Full name must not exceed 100 characters'),
    email: z
      .string()
      .min(1, 'Email is required')
      .email('Invalid email format')
      .max(100, 'Email must not exceed 100 characters'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(128, 'Password must not exceed 128 characters')
      .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
      .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
      .regex(/\d/, 'Password must contain at least one number')
      .regex(/[@$!%*?&#]/, 'Password must contain at least one special character (@$!%*?&#)'),
    confirmPassword: z.string().min(1, 'Confirm password is required'),
    acceptTerms: z.boolean().refine((val) => val === true, {
      message: 'You must accept the terms and conditions',
    }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

type RegisterFields = z.infer<typeof registerSchema>;

export const RegisterForm: React.FC = () => {
  const registerMutation = useRegister();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<RegisterFields>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      fullName: '',
      email: '',
      password: '',
      confirmPassword: '',
      acceptTerms: false,
    },
  });

  const passwordVal = watch('password');

  const onSubmit = (data: RegisterFields) => {
    registerMutation.mutate(
      {
        fullName: data.fullName,
        email: data.email,
        password: data.password,
      },
      {
        onSuccess: () => {
          toast.success('Account created successfully! Check your email to verify your account.');
          navigate('/login');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Registration failed. Try checking if email already exists.');
        },
      }
    );
  };

  return (
    <AuthCard>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        {/* Full Name */}
        <div className="flex flex-col gap-1.5 w-full">
          <label htmlFor="fullName" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
            Full Name
          </label>
          <input
            id="fullName"
            type="text"
            placeholder="John Doe"
            className={`w-full px-4 py-3 rounded-xl border transition-all outline-none text-sm bg-transparent ${
              errors.fullName
                ? 'border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500'
                : 'border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600 text-slate-900 dark:text-slate-50'
            }`}
            {...register('fullName')}
          />
          {errors.fullName && (
            <span className="text-xs font-medium text-red-500">
              {errors.fullName.message}
            </span>
          )}
        </div>

        {/* Email */}
        <div className="flex flex-col gap-1.5 w-full">
          <label htmlFor="email" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
            Email Address
          </label>
          <input
            id="email"
            type="email"
            placeholder="you@example.com"
            className={`w-full px-4 py-3 rounded-xl border transition-all outline-none text-sm bg-transparent ${
              errors.email
                ? 'border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500'
                : 'border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600 text-slate-900 dark:text-slate-50'
            }`}
            {...register('email')}
          />
          {errors.email && (
            <span className="text-xs font-medium text-red-500">
              {errors.email.message}
            </span>
          )}
        </div>

        {/* Password */}
        <PasswordInput
          label="Password"
          placeholder="••••••••"
          error={errors.password?.message}
          register={register('password')}
        />

        {/* Password Strength */}
        <PasswordStrength password={passwordVal} />

        {/* Confirm Password */}
        <PasswordInput
          label="Confirm Password"
          placeholder="••••••••"
          error={errors.confirmPassword?.message}
          register={register('confirmPassword')}
        />

        {/* Accept Terms */}
        <div className="flex flex-col gap-1 mt-1">
          <label className="flex items-start gap-2 text-xs text-slate-600 dark:text-slate-400 cursor-pointer select-none">
            <input
              type="checkbox"
              className="mt-0.5 w-16 h-16 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer"
              {...register('acceptTerms')}
            />
            <span>
              I accept the{' '}
              <a href="#" className="text-indigo-600 dark:text-indigo-400 hover:underline">
                Terms and Conditions
              </a>{' '}
              and{' '}
              <a href="#" className="text-indigo-600 dark:text-indigo-400 hover:underline">
                Privacy Policy
              </a>
            </span>
          </label>
          {errors.acceptTerms && (
            <span className="text-xs font-medium text-red-500">
              {errors.acceptTerms.message}
            </span>
          )}
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={registerMutation.isPending}
          className="w-full mt-4 py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold text-sm transition-all shadow-md shadow-indigo-600/10 hover:shadow-indigo-600/20 active:scale-[0.98] flex items-center justify-center gap-2"
        >
          {registerMutation.isPending ? 'Registering...' : 'Register'}
        </button>
      </form>

      {/* Login link */}
      <div className="text-center text-xs text-slate-500 dark:text-slate-400 border-t border-slate-100 dark:border-slate-800 pt-4">
        Already have an account?{' '}
        <Link
          to="/login"
          className="text-indigo-600 dark:text-indigo-400 font-bold hover:underline"
        >
          Log in
        </Link>
      </div>
    </AuthCard>
  );
};
