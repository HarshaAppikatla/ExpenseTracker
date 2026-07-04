import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Mail, ArrowLeft, CheckCircle2 } from 'lucide-react';
import { useForgotPassword } from '../hooks/useAuth';
import { AuthCard } from './AuthCard';

const forgotPasswordSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email format'),
});

type ForgotPasswordFields = z.infer<typeof forgotPasswordSchema>;

export const ForgotPasswordForm: React.FC = () => {
  const [isSubmitted, setIsSubmitted] = useState(false);
  const forgotPasswordMutation = useForgotPassword();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFields>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: {
      email: '',
    },
  });

  const onSubmit = (data: ForgotPasswordFields) => {
    forgotPasswordMutation.mutate(data.email, {
      onSuccess: () => {
        setIsSubmitted(true);
        toast.success('Password reset link sent!');
      },
      onError: (err: any) => {
        toast.error(err.message || 'Failed to send reset link.');
      },
    });
  };

  if (isSubmitted) {
    return (
      <AuthCard>
        <div className="flex flex-col items-center text-center gap-4 py-4">
          <CheckCircle2 className="w-16 h-16 text-green-500" />
          <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">
            Check Your Email
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 max-w-[320px]">
            If that email is registered, we have sent a password reset link to your inbox.
          </p>
          <Link
            to="/login"
            className="mt-6 flex items-center justify-center gap-2 text-sm font-semibold text-indigo-600 dark:text-indigo-400 hover:underline"
          >
            <ArrowLeft size={16} />
            Back to login
          </Link>
        </div>
      </AuthCard>
    );
  }

  return (
    <AuthCard>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        <div className="flex flex-col gap-1.5 w-full">
          <label htmlFor="email" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
            Email Address
          </label>
          <div className="relative">
            <input
              id="email"
              type="email"
              placeholder="you@example.com"
              className={`w-full px-4 py-3 pl-10 rounded-xl border transition-all outline-none text-sm bg-transparent ${
                errors.email
                  ? 'border-red-500 focus:border-red-500 focus:ring-1 focus:ring-red-500'
                  : 'border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600 text-slate-900 dark:text-slate-50'
              }`}
              {...register('email')}
            />
            <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-16 h-16 text-slate-400" />
          </div>
          {errors.email && (
            <span className="text-xs font-medium text-red-500">
              {errors.email.message}
            </span>
          )}
        </div>

        <button
          type="submit"
          disabled={forgotPasswordMutation.isPending}
          className="w-full mt-4 py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold text-sm transition-all shadow-md shadow-indigo-600/10 hover:shadow-indigo-600/20 active:scale-[0.98] flex items-center justify-center gap-2"
        >
          {forgotPasswordMutation.isPending ? 'Sending Link...' : 'Send Reset Link'}
        </button>
      </form>

      <div className="text-center text-xs border-t border-slate-100 dark:border-slate-800 pt-4">
        <Link
          to="/login"
          className="flex items-center justify-center gap-2 text-slate-500 hover:text-indigo-600 dark:text-slate-400 dark:hover:text-indigo-400 transition-colors font-medium"
        >
          <ArrowLeft size={14} />
          Back to login
        </Link>
      </div>
    </AuthCard>
  );
};
