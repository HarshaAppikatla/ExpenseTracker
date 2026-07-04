import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { CategoriesPage } from '../CategoriesPage';
import { useCategories, useCreateCategory, useDeleteCategory } from '@/features/category/hooks/useCategories';

// Mock category hooks
vi.mock('@/features/category/hooks/useCategories', () => ({
  useCategories: vi.fn(),
  useCreateCategory: vi.fn(),
  useDeleteCategory: vi.fn(),
}));

describe('CategoriesPage Component', () => {
  const mockCreateMutate = vi.fn();
  const mockDeleteMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    (useCategories as any).mockReturnValue({
      data: [
        { id: 'cat-sys-1', name: 'Food', icon: 'restaurant', color: '#FF5733', systemCategory: true },
        { id: 'cat-cust-1', name: 'Gifts', icon: 'card_giftcard', color: '#1F75FE', systemCategory: false },
      ],
      isLoading: false,
    });

    (useCreateCategory as any).mockReturnValue({
      mutate: mockCreateMutate,
      isPending: false,
    });

    (useDeleteCategory as any).mockReturnValue({
      mutate: mockDeleteMutate,
      isPending: false,
    });
  });

  it('renders system and custom categories successfully', () => {
    render(
      <MemoryRouter>
        <CategoriesPage />
      </MemoryRouter>
    );

    expect(screen.getByText('Food')).toBeInTheDocument();
    expect(screen.getByText('Gifts')).toBeInTheDocument();
    expect(screen.getByText('System Default')).toBeInTheDocument();
    expect(screen.getByText('Custom')).toBeInTheDocument();
  });

  it('submits a new custom category successfully', async () => {
    render(
      <MemoryRouter>
        <CategoriesPage />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText(/e.g. Groceries/i), {
      target: { value: 'Books' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Create Category/i }));

    await waitFor(() => {
      expect(mockCreateMutate).toHaveBeenCalledWith(
        { name: 'Books', icon: 'restaurant', color: '#FF5733' },
        expect.any(Object)
      );
    });
  });
});
