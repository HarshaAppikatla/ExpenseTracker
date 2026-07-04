import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useLogin } from '../hooks/useAuth';
import { PasswordInput } from './PasswordInput';
import { AuthCard } from './AuthCard';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email format'),
  password: z.string().min(1, 'Password is required'),
  rememberMe: z.boolean().optional(),
});

type LoginFields = z.infer<typeof loginSchema>;

export const LoginForm: React.FC = () => {
  const { login } = useAuthContext();
  const loginMutation = useLogin();
  const navigate = useNavigate();
  const location = useLocation();

  // Redirect target: where they came from, or dashboard by default
  const from = (location.state as any)?.from?.pathname || '/dashboard';

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<LoginFields>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
      rememberMe: false,
    },
  });

  // Load remembered email on mount
  useEffect(() => {
    const rememberedEmail = localStorage.getItem('remembered_email');
    if (rememberedEmail) {
      setValue('email', rememberedEmail);
      setValue('rememberMe', true);
    }
  }, [setValue]);

  const onSubmit = (data: LoginFields) => {
    loginMutation.mutate(
      { email: data.email, password: data.password },
      {
        onSuccess: (res) => {
          if (data.rememberMe) {
            localStorage.setItem('remembered_email', data.email);
          } else {
            localStorage.removeItem('remembered_email');
          }
          login(res);
          toast.success('Welcome back to ExpenseFlow!');
          navigate(from, { replace: true });
        },
        onError: (err: any) => {
          toast.error(err.message || 'Login failed. Please verify credentials.');
        },
      }
    );
  };

  return (
    <AuthCard>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        {/* Email Input */}
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

        {/* Password Input */}
        <PasswordInput
          label="Password"
          placeholder="••••••••"
          error={errors.password?.message}
          register={register('password')}
        />

        {/* Remember Me & Forgot Password */}
        <div className="flex items-center justify-between text-xs font-medium mt-1">
          <label className="flex items-center gap-2 text-slate-600 dark:text-slate-400 cursor-pointer select-none">
            <input
              type="checkbox"
              className="w-16 h-16 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer"
              {...register('rememberMe')}
            />
            Remember me
          </label>
          <Link
            to="/forgot-password"
            className="text-indigo-600 dark:text-indigo-400 hover:underline"
          >
            Forgot password?
          </Link>
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={loginMutation.isPending}
          className="w-full mt-4 py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold text-sm transition-all shadow-md shadow-indigo-600/10 hover:shadow-indigo-600/20 active:scale-[0.98] flex items-center justify-center gap-2"
        >
          {loginMutation.isPending ? 'Logging in...' : 'Log in'}
        </button>
      </form>

      {/* Registration link */}
      <div className="text-center text-xs text-slate-500 dark:text-slate-400 border-t border-slate-100 dark:border-slate-800 pt-4">
        Don't have an account?{' '}
        <Link
          to="/register"
          className="text-indigo-600 dark:text-indigo-400 font-bold hover:underline"
        >
          Create one
        </Link>
      </div>
    </AuthCard>
  );
};
