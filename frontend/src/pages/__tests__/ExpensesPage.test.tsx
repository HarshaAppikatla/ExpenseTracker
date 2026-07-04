import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { ExpensesPage } from '../ExpensesPage';
import { useExpenses, useCreateExpense, useDeleteExpense, useMerchantSuggestions, useUpdateExpense } from '@/features/expense/hooks/useExpenses';
import { useCategories } from '@/features/category/hooks/useCategories';
import { useProfile } from '@/features/profile/hooks/useProfile';
import { useDashboardSummary } from '@/features/dashboard/hooks/useDashboard';

// Mock all hooks
vi.mock('@/features/expense/hooks/useExpenses', () => ({
  useExpenses: vi.fn(),
  useCreateExpense: vi.fn(),
  useUpdateExpense: vi.fn(),
  useDeleteExpense: vi.fn(),
  useMerchantSuggestions: vi.fn(),
}));

vi.mock('@/features/category/hooks/useCategories', () => ({
  useCategories: vi.fn(),
}));

vi.mock('@/features/profile/hooks/useProfile', () => ({
  useProfile: vi.fn(),
}));

vi.mock('@/features/receipt/hooks/useReceipts', () => ({
  useUploadReceipt: vi.fn(),
  useDeleteReceipt: vi.fn(),
}));

vi.mock('@/features/dashboard/hooks/useDashboard', () => ({
  useDashboardSummary: vi.fn(),
}));

describe('ExpensesPage Component', () => {
  const mockCreateMutate = vi.fn();
  const mockDeleteMutate = vi.fn();

  beforeEach(async () => {
    vi.clearAllMocks();

    (useProfile as any).mockReturnValue({
      data: { preferredCurrency: '$' },
    });

    (useDashboardSummary as any).mockReturnValue({
      data: {
        openingBalance: 1000,
        totalIncome: 500,
        totalExpenses: 25.50,
        netBalance: 1474.50,
        categoryBreakdown: [{ name: 'Food', color: '#FF5733', amount: 25.50 }],
        recentTransactions: [],
      },
    });

    (useCategories as any).mockReturnValue({
      data: [{ id: 'cat-1', name: 'Food', icon: 'restaurant', color: '#FF5733', systemCategory: true }],
    });

    (useExpenses as any).mockReturnValue({
      data: {
        content: [
          {
            id: 'exp-1',
            amount: 25.50,
            currencyCode: 'USD',
            expenseDate: '2026-06-28T12:00:00Z',
            merchant: 'Starbucks',
            category: { name: 'Food', color: '#FF5733' },
          },
        ],
        totalPages: 1,
        totalElements: 1,
      },
      isLoading: false,
    });

    (useCreateExpense as any).mockReturnValue({
      mutate: mockCreateMutate,
      isPending: false,
    });

    (useUpdateExpense as any).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    });

    (useDeleteExpense as any).mockReturnValue({
      mutate: mockDeleteMutate,
      isPending: false,
    });

    (useMerchantSuggestions as any).mockReturnValue({
      data: [],
    });

    const { useUploadReceipt, useDeleteReceipt } = await import('@/features/receipt/hooks/useReceipts');
    (useUploadReceipt as any).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    });
    (useDeleteReceipt as any).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    });
  });

  it('renders expense items and details successfully', () => {
    render(
      <MemoryRouter>
        <ExpensesPage />
      </MemoryRouter>
    );

    // 'Starbucks' / '$ 25.50' may appear in both desktop and mobile card views
    expect(screen.getAllByText('Starbucks').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('$ 25.50').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByRole('button', { name: /Add Expense/i })).toBeInTheDocument();
  });

  it('submits a new expense successfully', async () => {
    render(
      <MemoryRouter>
        <ExpensesPage />
      </MemoryRouter>
    );

    // Open Form Modal
    fireEvent.click(screen.getByRole('button', { name: /Add Expense/i }));

    // Fill form — labels now have ids matching expense-amount, expense-category, expense-merchant
    fireEvent.change(screen.getByLabelText(/Amount/i), {
      target: { value: '50.00' },
    });
    fireEvent.change(screen.getByLabelText(/^Category$/i), {
      target: { value: 'cat-1' },
    });
    fireEvent.change(screen.getByLabelText(/^Merchant$/i), {
      target: { value: 'Walmart' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Record Expense/i }));

    await waitFor(() => {
      expect(mockCreateMutate).toHaveBeenCalledWith(
        expect.objectContaining({
          categoryId: 'cat-1',
          amount: 50.00,
          merchant: 'Walmart',
        }),
        expect.any(Object)
      );
    });
  });
});
