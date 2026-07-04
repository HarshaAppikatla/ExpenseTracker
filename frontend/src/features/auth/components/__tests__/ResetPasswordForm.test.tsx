import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { ResetPasswordForm } from '../ResetPasswordForm';
import { useResetPassword } from '../../hooks/useAuth';

// Mock the hook
vi.mock('../../hooks/useAuth', () => ({
  useResetPassword: vi.fn(),
}));

describe('ResetPasswordForm Component', () => {
  const mockMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    (useResetPassword as any).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    });
  });

  it('renders input fields and reset password successfully', async () => {
    render(
      <MemoryRouter initialEntries={['/reset-password?token=my-reset-token']}>
        <ResetPasswordForm />
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/^New Password$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Confirm New Password$/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Reset Password/i })).toBeInTheDocument();

    const pwdInputs = screen.getAllByPlaceholderText(/••••••••/i);
    fireEvent.change(pwdInputs[0], {
      target: { value: 'NewPassword123!' },
    });
    fireEvent.change(pwdInputs[1], {
      target: { value: 'NewPassword123!' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Reset Password/i }));

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        { token: 'my-reset-token', password: 'NewPassword123!' },
        expect.any(Object)
      );
    });
  });
});
