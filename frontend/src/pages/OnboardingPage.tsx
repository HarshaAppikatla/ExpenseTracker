import React from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useOnboard } from '@/features/profile/hooks/useProfile';
import { AuthLayout } from '@/features/auth/components/AuthLayout';
import { AuthCard } from '@/features/auth/components/AuthCard';

const onboardingSchema = z.object({
  preferredCurrency: z.string().min(1, 'Please select a preferred currency'),
  openingBalance: z
    .number({ invalid_type_error: 'Opening balance must be a number' })
    .nonnegative('Opening balance cannot be negative'),
  initialMonthlyIncome: z
    .number({ invalid_type_error: 'Monthly income must be a number' })
    .nonnegative('Monthly income cannot be negative')
    .nullable()
    .optional(),
});

type OnboardingFields = z.infer<typeof onboardingSchema>;

const CURRENCIES = [
  { code: 'USD', name: 'US Dollar ($)' },
  { code: 'EUR', name: 'Euro (€)' },
  { code: 'GBP', name: 'British Pound (£)' },
  { code: 'INR', name: 'Indian Rupee (₹)' },
  { code: 'JPY', name: 'Japanese Yen (¥)' },
  { code: 'CAD', name: 'Canadian Dollar (C$)' },
  { code: 'AUD', name: 'Australian Dollar (A$)' },
];

export const OnboardingPage: React.FC = () => {
  const onboardMutation = useOnboard();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<OnboardingFields>({
    resolver: zodResolver(onboardingSchema),
    defaultValues: {
      preferredCurrency: 'USD',
      openingBalance: 0,
      initialMonthlyIncome: null,
    },
  });

  const onSubmit = (data: OnboardingFields) => {
    onboardMutation.mutate(data, {
      onSuccess: () => {
        toast.success('Onboarding completed successfully! Welcome to ExpenseFlow.');
        navigate('/dashboard', { replace: true });
      },
      onError: (err: any) => {
        toast.error(err.message || 'Onboarding failed. Please try again.');
      },
    });
  };

  const handleSkip = () => {
    // Skip uses defaults: USD currency, 0 opening balance, no initial income
    onboardMutation.mutate(
      {
        preferredCurrency: 'USD',
        openingBalance: 0,
        initialMonthlyIncome: null,
      },
      {
        onSuccess: () => {
          toast.success('Onboarding skipped. You can configure preferences in Settings.');
          navigate('/dashboard', { replace: true });
        },
        onError: (err: any) => {
          toast.error(err.message || 'Failed to skip onboarding. Please try again.');
        },
      }
    );
  };

  return (
    <AuthLayout
      title="Complete Your Setup"
      subtitle="Configure your currency and starting balance to personalize your ledger workspace"
    >
      <AuthCard>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          {/* Preferred Currency Selector */}
          <div className="flex flex-col gap-1.5 w-full">
            <label htmlFor="preferredCurrency" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              Preferred Currency
            </label>
            <select
              id="preferredCurrency"
              className={`w-full px-4 py-3 rounded-xl border transition-all outline-none text-sm bg-transparent ${
                errors.preferredCurrency
                  ? 'border-red-500 focus:border-red-500 focus:ring-1'
                  : 'border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600 text-slate-900 dark:text-slate-50'
              }`}
              {...register('preferredCurrency')}
            >
              {CURRENCIES.map((c) => (
                <option key={c.code} value={c.code} className="dark:bg-slate-900 text-slate-900 dark:text-slate-50">
                  {c.name}
                </option>
              ))}
            </select>
            {errors.preferredCurrency && (
              <span className="text-xs font-medium text-red-500">
                {errors.preferredCurrency.message}
              </span>
            )}
          </div>

          {/* Opening Balance */}
          <div className="flex flex-col gap-1.5 w-full">
            <label htmlFor="openingBalance" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              Opening Available Balance (Optional)
            </label>
            <input
              id="openingBalance"
              type="number"
              step="0.01"
              placeholder="0.00"
              className={`w-full px-4 py-3 rounded-xl border transition-all outline-none text-sm bg-transparent ${
                errors.openingBalance
                  ? 'border-red-500 focus:border-red-500 focus:ring-1'
                  : 'border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600 text-slate-900 dark:text-slate-50'
              }`}
              {...register('openingBalance', { valueAsNumber: true })}
            />
            {errors.openingBalance && (
              <span className="text-xs font-medium text-red-500">
                {errors.openingBalance.message}
              </span>
            )}
          </div>

          {/* Initial Monthly Income */}
          <div className="flex flex-col gap-1.5 w-full">
            <label htmlFor="initialMonthlyIncome" className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              Estimated Monthly Salary (Optional)
            </label>
            <input
              id="initialMonthlyIncome"
              type="number"
              step="0.01"
              placeholder="0.00"
              className={`w-full px-4 py-3 rounded-xl border transition-all outline-none text-sm bg-transparent ${
                errors.initialMonthlyIncome
                  ? 'border-red-500 focus:border-red-500 focus:ring-1'
                  : 'border-slate-200 dark:border-slate-800 focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600 text-slate-900 dark:text-slate-50'
              }`}
              onChange={(e) => {
                const val = e.target.value;
                setValue('initialMonthlyIncome', val === '' ? null : parseFloat(val));
              }}
            />
            {errors.initialMonthlyIncome && (
              <span className="text-xs font-medium text-red-500">
                {errors.initialMonthlyIncome.message}
              </span>
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex flex-col gap-2.5 mt-4">
            <button
              type="submit"
              disabled={onboardMutation.isPending}
              className="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold text-sm transition-all shadow-md shadow-indigo-600/10 active:scale-[0.98] flex items-center justify-center"
            >
              {onboardMutation.isPending ? 'Completing Setup...' : 'Finish Setup'}
            </button>
            <button
              type="button"
              onClick={handleSkip}
              disabled={onboardMutation.isPending}
              className="w-full py-3 px-4 rounded-xl border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-900 font-semibold text-sm transition-all active:scale-[0.98] flex items-center justify-center"
            >
              Skip Setup
            </button>
          </div>
        </form>
      </AuthCard>
    </AuthLayout>
  );
};
export default OnboardingPage;
