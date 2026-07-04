import React from 'react';
import { AuthLayout } from '@/features/auth/components/AuthLayout';
import { LoginForm } from '@/features/auth/components/LoginForm';

export const LoginPage: React.FC = () => {
  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to track spending and manage trip budgets with your groups"
    >
      <LoginForm />
    </AuthLayout>
  );
};
export default LoginPage;
