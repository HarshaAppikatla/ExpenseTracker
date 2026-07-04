import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { LandingPage } from '../pages/LandingPage';
import { DashboardLayout } from '../layouts/DashboardLayout';
import { SettingsLayout } from '../layouts/SettingsLayout';
import { DashboardPage } from '../pages/DashboardPage';
import { ProfilePage } from '../pages/ProfilePage';
import { SettingsPage } from '../pages/SettingsPage';
import { SettingsPlanningPage } from '../pages/SettingsPlanningPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { OnboardingPage } from '../pages/OnboardingPage';
import { ExpensesPage } from '../pages/ExpensesPage';
import { CategoriesPage } from '../pages/CategoriesPage';
import { IncomePage } from '../pages/IncomePage';
import { TransactionsPage } from '../pages/TransactionsPage';
import { BudgetPage } from '../pages/BudgetPage';
import { BudgetDetailPage } from '../pages/BudgetDetailPage';
import { SavingsPage } from '../pages/SavingsPage';
import { SavingsDetailPage } from '../pages/SavingsDetailPage';
import { RecurringPage } from '../pages/RecurringPage';
import { RecurringDetailPage } from '../pages/RecurringDetailPage';
import { NotificationsPage } from '../pages/NotificationsPage';
import { InsightsPage } from '../pages/InsightsPage';
import { GroupsPage } from '../pages/GroupsPage';
import { GroupDetailPage } from '../pages/GroupDetailPage';

// Auth Pages & Guards
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { ForgotPasswordPage } from '../pages/ForgotPasswordPage';
import { ResetPasswordPage } from '../pages/ResetPasswordPage';
import { VerifyEmailPage } from '../pages/VerifyEmailPage';
import { ProtectedRoute } from '../components/auth/ProtectedRoute';

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      
      {/* Auth Public Routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />

      {/* Onboarding protected route */}
      <Route
        path="/onboarding"
        element={
          <ProtectedRoute>
            <OnboardingPage />
          </ProtectedRoute>
        }
      />

      {/* Protected Layout & Routes */}
      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/expenses" element={<ExpensesPage />} />
        <Route path="/income" element={<IncomePage />} />
        <Route path="/transactions" element={<TransactionsPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
        <Route path="/budget" element={<BudgetPage />} />
        <Route path="/budgets" element={<BudgetPage />} />
        <Route path="/budgets/new" element={<BudgetPage />} />
        <Route path="/budgets/:id" element={<BudgetDetailPage />} />
        <Route path="/savings" element={<SavingsPage />} />
        <Route path="/savings/new" element={<SavingsPage />} />
        <Route path="/savings/:id" element={<SavingsDetailPage />} />
        <Route path="/recurring" element={<RecurringPage />} />
        <Route path="/recurring/new" element={<RecurringPage />} />
        <Route path="/recurring/:id" element={<RecurringDetailPage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
        <Route path="/insights" element={<InsightsPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/groups" element={<GroupsPage />} />
        <Route path="/groups/:id" element={<GroupDetailPage />} />

        {/* Settings nested under SettingsLayout */}
        <Route path="/settings" element={<SettingsLayout />}>
          <Route index element={<SettingsPage />} />
          <Route path="planning" element={<SettingsPlanningPage />} />
        </Route>
      </Route>
      
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

