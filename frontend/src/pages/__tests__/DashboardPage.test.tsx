import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { DashboardPage } from '../DashboardPage';
import { useDashboardSummary, useFinancialDashboard } from '@/features/dashboard/hooks/useDashboard';
import { useProfile } from '@/features/profile/hooks/useProfile';

vi.mock('@/features/dashboard/hooks/useDashboard', () => ({
  useDashboardSummary: vi.fn(),
  useFinancialDashboard: vi.fn(),
}));

vi.mock('@/features/profile/hooks/useProfile', () => ({
  useProfile: vi.fn(),
}));

vi.mock('@/hooks/useAuthContext', () => ({
  useAuthContext: () => ({
    user: { fullName: 'Harsha Appikatla' },
  }),
}));

describe('DashboardPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    (useProfile as any).mockReturnValue({
      data: { preferredCurrency: '$' },
    });

    (useFinancialDashboard as any).mockReturnValue({
      data: {
        totalIncomeCurrentMonth: 500.00,
        totalSpentCurrentMonth: 200.00,
        netSavingsCurrentMonth: 300.00,
        budgetLimitTotal: 400.00,
        budgetSpentTotal: 150.00,
        budgetUtilizationRate: 37.5,
        savingsGoalsProgress: [],
        topSpendingCategories: [],
        monthlyTrends: [],
      },
      isLoading: false,
      isError: false,
    });

    (useDashboardSummary as any).mockReturnValue({
      data: {
        openingBalance: 1000.00,
        totalIncome: 500.00,
        totalExpenses: 200.00,
        netBalance: 1300.00,
        categoryBreakdown: [
          { name: 'Food', color: '#FF5733', amount: 150.00 },
        ],
        recentTransactions: [
          {
            id: 'tx-1',
            type: 'EXPENSE',
            amount: 50.00,
            currencyCode: 'USD',
            date: '2026-06-28T12:00:00Z',
            categoryName: 'Food',
            categoryColor: '#FF5733',
            sourceOrMerchant: 'Starbucks',
          },
        ],
      },
      isLoading: false,
      isError: false,
    });
  });

  it('renders bento summaries and dynamic metrics correctly', () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText('Total Net Balance')).toBeInTheDocument();
    expect(screen.getByText('$ 1300.00')).toBeInTheDocument();
    expect(screen.getByText('+ $ 500.00')).toBeInTheDocument();
    expect(screen.getByText('- $ 200.00')).toBeInTheDocument();
    expect(screen.getByText('Starbucks')).toBeInTheDocument();
  });
});
