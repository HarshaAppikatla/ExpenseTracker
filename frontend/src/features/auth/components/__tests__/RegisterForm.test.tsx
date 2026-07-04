import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { RegisterForm } from '../RegisterForm';
import { useRegister } from '../../hooks/useAuth';

// Mock the hook
vi.mock('../../hooks/useAuth', () => ({
  useRegister: vi.fn(),
}));

describe('RegisterForm Component', () => {
  const mockMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    (useRegister as any).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    });
  });

  it('renders inputs and validations', () => {
    render(
      <MemoryRouter>
        <RegisterForm />
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/Full Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Email Address/i)).toBeInTheDocument();
    expect(screen.getAllByLabelText(/Password/i)[0]).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Register/i })).toBeInTheDocument();
  });

  it('validates fields and shows error messages on empty submit', async () => {
    render(
      <MemoryRouter>
        <RegisterForm />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: /Register/i }));

    await waitFor(() => {
      expect(screen.getByText(/Full name is required/i)).toBeInTheDocument();
      expect(screen.getByText(/Email is required/i)).toBeInTheDocument();
    });
  });

  it('submits registration successfully when form is valid', async () => {
    render(
      <MemoryRouter>
        <RegisterForm />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText(/John Doe/i), {
      target: { value: 'Jane Doe' },
    });
    fireEvent.change(screen.getByPlaceholderText(/you@example.com/i), {
      target: { value: 'jane.doe@example.com' },
    });
    // Note: Password inputs are rendered via PasswordInput, placeholder is ••••••••
    const pwdInputs = screen.getAllByPlaceholderText(/••••••••/i);
    fireEvent.change(pwdInputs[0], {
      target: { value: 'Password123!' },
    });
    fireEvent.change(pwdInputs[1], {
      target: { value: 'Password123!' },
    });

    // Accept terms
    fireEvent.click(screen.getByRole('checkbox'));

    fireEvent.click(screen.getByRole('button', { name: /Register/i }));

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        { fullName: 'Jane Doe', email: 'jane.doe@example.com', password: 'Password123!' },
        expect.any(Object)
      );
    });
  });
});
