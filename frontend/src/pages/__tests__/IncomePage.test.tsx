import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { IncomePage } from '../IncomePage';
import { useIncomeList, useCreateIncome, useDeleteIncome, useUpdateIncome } from '@/features/income/hooks/useIncome';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { useDashboardSummary } from '@/features/dashboard/hooks/useDashboard';

// Mock all hooks
vi.mock('@/features/income/hooks/useIncome', () => ({
  useIncomeList: vi.fn(),
  useCreateIncome: vi.fn(),
  useUpdateIncome: vi.fn(),
  useDeleteIncome: vi.fn(),
}));

vi.mock('@/features/profile/hooks/useProfile', () => ({
  useProfile: vi.fn(),
}));

vi.mock('@/features/dashboard/hooks/useDashboard', () => ({
  useDashboardSummary: vi.fn(),
}));

describe('IncomePage Component', () => {
  const mockCreateMutate = vi.fn();
  const mockDeleteMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    (useProfile as any).mockReturnValue({
      data: { preferredCurrency: '$' },
    });

    (useDashboardSummary as any).mockReturnValue({
      data: {
        openingBalance: 1000,
        totalIncome: 5000.00,
        totalExpenses: 25.50,
        netBalance: 5974.50,
        categoryBreakdown: [],
        recentTransactions: [],
      },
    });

    (useIncomeList as any).mockReturnValue({
      data: {
        content: [
          {
            id: 'inc-1',
            amount: 5000.00,
            currencyCode: 'USD',
            incomeDate: '2026-06-28T10:00:00Z',
            source: 'Salary',
            description: 'Monthly salary check',
          },
        ],
        totalPages: 1,
        totalElements: 1,
      },
      isLoading: false,
    });

    (useCreateIncome as any).mockReturnValue({
      mutate: mockCreateMutate,
      isPending: false,
    });

    (useUpdateIncome as any).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    });

    (useDeleteIncome as any).mockReturnValue({
      mutate: mockDeleteMutate,
      isPending: false,
    });
  });

  it('renders income ledger list successfully', () => {
    render(
      <MemoryRouter>
        <IncomePage />
      </MemoryRouter>
    );

    // 'Salary' may appear in both desktop table and mobile card (CSS hidden), so use getAllByText
    expect(screen.getAllByText('Salary').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('+ $ 5000.00').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByRole('button', { name: /Add Income/i })).toBeInTheDocument();
  });

  it('submits a new income successfully', async () => {
    render(
      <MemoryRouter>
        <IncomePage />
      </MemoryRouter>
    );

    // Open Form modal
    fireEvent.click(screen.getByRole('button', { name: /Add Income/i }));

    // Fill form — label is "Amount ($)" so match with partial
    fireEvent.change(screen.getByLabelText(/Amount/i), {
      target: { value: '250.00' },
    });
    fireEvent.change(screen.getByLabelText(/^Source$/i), {
      target: { value: 'Consulting' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Record Income/i }));

    await waitFor(() => {
      expect(mockCreateMutate).toHaveBeenCalledWith(
        expect.objectContaining({
          amount: 250.00,
          source: 'Consulting',
        }),
        expect.any(Object)
      );
    });
  });
});
