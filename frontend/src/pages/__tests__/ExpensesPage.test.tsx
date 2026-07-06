import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { ExpensesPage } from '../ExpensesPage';
import { useExpenses } from '@/features/expense/hooks/useExpenses';
import { useProfile } from '@/features/profile/hooks/useProfile';

// Mock all hooks
vi.mock('@/features/expense/hooks/useExpenses', () => ({
  useExpenses: vi.fn(),
}));

vi.mock('@/features/profile/hooks/useProfile', () => ({
  useProfile: vi.fn(),
}));

describe('ExpensesPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    (useProfile as any).mockReturnValue({
      data: { id: 'profile-1', preferredCurrency: '$' },
    });

    (useExpenses as any).mockReturnValue({
      data: {
        content: [
          {
            id: 'exp-1',
            amount: 25.50,
            currency: 'USD',
            expenseDate: '2026-06-28T12:00:00Z',
            description: 'Starbucks Coffee',
            category: 'FOOD',
            paidByUserId: 'profile-1',
            paidByUserName: 'John Doe',
            status: 'POSTED',
            splitType: 'EQUAL',
            splits: [
              {
                id: 'split-1',
                userId: 'profile-1',
                userName: 'John Doe',
                userEmail: 'john@example.com',
                owedAmount: 25.50,
                allocationValue: 1.0,
              }
            ],
          },
        ],
        totalPages: 1,
        totalElements: 1,
      },
      isLoading: false,
      isError: false,
    });
  });

  it('renders expense ledger and items successfully', () => {
    render(
      <MemoryRouter>
        <ExpensesPage />
      </MemoryRouter>
    );

    // Verify page header
    expect(screen.getByText('Expenses Ledger')).toBeInTheDocument();
    
    // Verify item description, amount, and payer
    expect(screen.getByText('Starbucks Coffee')).toBeInTheDocument();
    expect(screen.getAllByText('USD 25.50')[0]).toBeInTheDocument();
    expect(screen.getByText('You')).toBeInTheDocument();
  });

  it('expands rows and shows split details on toggle click', () => {
    render(
      <MemoryRouter>
        <ExpensesPage />
      </MemoryRouter>
    );

    // Row details initially hidden
    expect(screen.queryByText(/Split Details/i)).not.toBeInTheDocument();

    // Click toggle button
    const toggleBtn = screen.getByTitle('Toggle details');
    fireEvent.click(toggleBtn);

    // Verify row details expanded
    expect(screen.getByText(/Split Details/i)).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
  });
});
