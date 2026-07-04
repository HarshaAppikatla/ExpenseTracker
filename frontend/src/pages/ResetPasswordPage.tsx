import React from 'react';
import { AuthLayout } from '@/features/auth/components/AuthLayout';
import { ResetPasswordForm } from '@/features/auth/components/ResetPasswordForm';

export const ResetPasswordPage: React.FC = () => {
  return (
    <AuthLayout
      title="Create new password"
      subtitle="Configure a secure new password for your ExpenseFlow account"
    >
      <ResetPasswordForm />
    </AuthLayout>
  );
};
export default ResetPasswordPage;
