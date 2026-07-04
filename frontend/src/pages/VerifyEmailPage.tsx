import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { CheckCircle2, XCircle, Loader2, ArrowRight } from 'lucide-react';
import toast from 'react-hot-toast';
import { AuthLayout } from '@/features/auth/components/AuthLayout';
import { AuthCard } from '@/features/auth/components/AuthCard';
import { useVerifyEmail, useResendVerification } from '@/features/auth/hooks/useAuth';

export const VerifyEmailPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const navigate = useNavigate();

  const verifyEmailMutation = useVerifyEmail();
  const resendMutation = useResendVerification();

  const [resendEmail, setResendEmail] = useState('');
  const [resendTriggered, setResendTriggered] = useState(false);

  useEffect(() => {
    if (token) {
      verifyEmailMutation.mutate(token, {
        onSuccess: () => {
          toast.success('Email verified successfully!');
        },
        onError: (err: any) => {
          toast.error(err.message || 'Verification failed. The token may be invalid or expired.');
        },
      });
    }
  }, [token]);

  const handleResend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!resendEmail) {
      toast.error('Email address is required');
      return;
    }

    resendMutation.mutate(resendEmail, {
      onSuccess: () => {
        setResendTriggered(true);
        toast.success('New verification email sent!');
      },
      onError: (err: any) => {
        toast.error(err.message || 'Failed to resend verification link.');
      },
    });
  };

  const renderContent = () => {
    if (!token) {
      return (
        <div className="flex flex-col items-center text-center gap-4 py-4">
          <XCircle className="w-16 h-16 text-red-500" />
          <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">
            Missing Verification Token
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 max-w-[320px]">
            Please click on the link provided in your registration email.
          </p>
          <button
            onClick={() => navigate('/login')}
            className="mt-6 flex items-center justify-center gap-2 text-sm font-semibold text-indigo-600 dark:text-indigo-400 hover:underline"
          >
            Back to login
          </button>
        </div>
      );
    }

    if (verifyEmailMutation.isPending) {
      return (
        <div className="flex flex-col items-center text-center gap-4 py-6">
          <Loader2 className="w-12 h-12 text-indigo-600 animate-spin" />
          <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">
            Verifying Your Account
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Please wait while we confirm your email address...
          </p>
        </div>
      );
    }

    if (verifyEmailMutation.isSuccess) {
      return (
        <div className="flex flex-col items-center text-center gap-4 py-4">
          <CheckCircle2 className="w-16 h-16 text-green-500" />
          <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">
            Email Verified!
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 max-w-[320px]">
            Your email has been confirmed. You can now log in to access your dashboard.
          </p>
          <button
            onClick={() => navigate('/login')}
            className="mt-6 w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm transition-all flex items-center justify-center gap-2"
          >
            Go to Login
            <ArrowRight size={16} />
          </button>
        </div>
      );
    }

    if (verifyEmailMutation.isError) {
      return (
        <div className="flex flex-col items-center text-center gap-4 py-4">
          <XCircle className="w-16 h-16 text-red-500" />
          <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">
            Verification Failed
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 max-w-[320px]">
            The link is invalid or has expired. Sprints require accounts to be verified.
          </p>

          {resendTriggered ? (
            <p className="text-xs text-green-600 dark:text-green-400 mt-4 font-semibold">
              Check your inbox! We've sent a new verification link.
            </p>
          ) : (
            <form onSubmit={handleResend} className="w-full mt-4 flex flex-col gap-3">
              <input
                type="email"
                placeholder="Enter your email"
                className="w-full px-4 py-2 text-sm border border-slate-200 dark:border-slate-800 rounded-xl bg-transparent outline-none focus:border-indigo-600"
                value={resendEmail}
                onChange={(e) => setResendEmail(e.target.value)}
              />
              <button
                type="submit"
                disabled={resendMutation.isPending}
                className="w-full py-2.5 px-4 bg-slate-800 hover:bg-slate-900 dark:bg-slate-200 dark:hover:bg-slate-100 dark:text-slate-950 text-white font-semibold text-xs rounded-xl transition-all"
              >
                {resendMutation.isPending ? 'Resending...' : 'Request New Link'}
              </button>
            </form>
          )}

          <button
            onClick={() => navigate('/login')}
            className="mt-4 text-xs font-semibold text-slate-500 hover:text-indigo-600 dark:text-slate-400 transition-colors"
          >
            Back to login
          </button>
        </div>
      );
    }

    return null;
  };

  return (
    <AuthLayout
      title="Verify account"
      subtitle="Complete your registration to unlock trip workspace tools"
    >
      <AuthCard>{renderContent()}</AuthCard>
    </AuthLayout>
  );
};
export default VerifyEmailPage;
