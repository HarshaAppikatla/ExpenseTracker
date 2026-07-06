import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { TransactionsPage } from '../TransactionsPage';
import { useTransactions } from '@/features/transaction/hooks/useTransactions';
import { useCategories } from '@/features/category/hooks/useCategories';
import { useProfile } from '@/features/profile/hooks/useProfile';

vi.mock('@/features/transaction/hooks/useTransactions', () => ({
  useTransactions: vi.fn(),
}));

vi.mock('@/features/category/hooks/useCategories', () => ({
  useCategories: vi.fn(),
}));

vi.mock('@/features/profile/hooks/useProfile', () => ({
  useProfile: vi.fn(),
}));

describe('TransactionsPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    (useProfile as any).mockReturnValue({
      data: { preferredCurrency: '$' },
    });

    (useCategories as any).mockReturnValue({
      data: [{ id: 'cat-1', name: 'Food' }],
    });

    (useTransactions as any).mockReturnValue({
      data: {
        content: [
          {
            id: 'tx-1',
            type: 'EXPENSE',
            amount: 15.00,
            currencyCode: 'USD',
            date: '2026-06-28T12:00:00Z',
            categoryName: 'Food',
            categoryColor: '#FF5733',
            sourceOrMerchant: 'McDonalds',
          },
        ],
        totalPages: 1,
        totalElements: 1,
      },
      isLoading: false,
    });
  });

  it('renders transactions ledger list and headers successfully', () => {
    render(
      <MemoryRouter>
        <TransactionsPage />
      </MemoryRouter>
    );

    expect(screen.getByText('McDonalds')).toBeInTheDocument();
    expect(screen.getByText('- $ 15.00')).toBeInTheDocument();
    expect(screen.getByText('EXPENSE')).toBeInTheDocument();
  });
});
