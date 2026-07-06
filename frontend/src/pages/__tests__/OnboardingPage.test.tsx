import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { OnboardingPage } from '../OnboardingPage';
import { useOnboard } from '@/features/profile/hooks/useProfile';

// Mock hook
vi.mock('@/features/profile/hooks/useProfile', () => ({
  useOnboard: vi.fn(),
}));

describe('OnboardingPage Component', () => {
  const mockMutate = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    (useOnboard as any).mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    });
  });

  it('renders dropdown and balance fields successfully', () => {
    render(
      <MemoryRouter>
        <OnboardingPage />
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/Preferred Currency/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Opening Available Balance/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Finish Setup/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Skip Setup/i })).toBeInTheDocument();
  });

  it('submits onboarding forms successfully when submitted', async () => {
    render(
      <MemoryRouter>
        <OnboardingPage />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/Preferred Currency/i), {
      target: { value: 'INR' },
    });
    fireEvent.change(screen.getByLabelText(/Opening Available Balance/i), {
      target: { value: '2500' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Finish Setup/i }));

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        { preferredCurrency: 'INR', openingBalance: 2500, initialMonthlyIncome: null },
        expect.any(Object)
      );
    });
  });

  it('submits default values when skipped', async () => {
    render(
      <MemoryRouter>
        <OnboardingPage />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: /Skip Setup/i }));

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        { preferredCurrency: 'USD', openingBalance: 0, initialMonthlyIncome: null },
        expect.any(Object)
      );
    });
  });
});
