import React from 'react';
import { AuthLayout } from '@/features/auth/components/AuthLayout';
import { RegisterForm } from '@/features/auth/components/RegisterForm';

export const RegisterPage: React.FC = () => {
  return (
    <AuthLayout
      title="Create account"
      subtitle="Sign up for free to split trip expenses and manage shared group budgets"
    >
      <RegisterForm />
    </AuthLayout>
  );
};
export default RegisterPage;
