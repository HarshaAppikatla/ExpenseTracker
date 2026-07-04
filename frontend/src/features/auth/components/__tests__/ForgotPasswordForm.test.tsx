import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { ForgotPasswordForm } from '../ForgotPasswordForm';
import { useForgotPassword } from '../../hooks/useAuth';

// Mock the hook
vi.mock('../../hooks/useAuth', () => ({
  useForgotPassword: vi.fn(),
}));

describe('ForgotPasswordForm Component', () => {
  const mockMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    (useForgotPassword as any).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    });
  });

  it('renders inputs and handles submission', async () => {
    render(
      <MemoryRouter>
        <ForgotPasswordForm />
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/Email Address/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Send Reset Link/i })).toBeInTheDocument();

    fireEvent.change(screen.getByPlaceholderText(/you@example.com/i), {
      target: { value: 'forgot@example.com' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Send Reset Link/i }));

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        'forgot@example.com',
        expect.any(Object)
      );
    });
  });
});
