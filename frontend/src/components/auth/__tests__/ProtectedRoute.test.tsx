import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { vi, describe, it, expect } from 'vitest';
import { ProtectedRoute } from '../ProtectedRoute';
import { useAuthContext } from '@/hooks/useAuthContext';
import { useProfile } from '@/features/profile/hooks/useProfile';

// Mock hook
vi.mock('@/hooks/useAuthContext', () => ({
  useAuthContext: vi.fn(),
}));

vi.mock('@/features/profile/hooks/useProfile', () => ({
  useProfile: vi.fn(),
}));

// Mock LoadingSpinner to avoid component dependency conflicts
vi.mock('@/components/feedback/LoadingSpinner', () => ({
  LoadingSpinner: () => <div data-testid="loading-spinner">Spinner</div>,
}));

describe('ProtectedRoute Component', () => {
  it('renders loading state when isLoading is true', () => {
    (useProfile as any).mockReturnValue({
      data: null,
      isLoading: false,
      error: null,
    });

    (useAuthContext as any).mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
    });

    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('redirects to login when user is unauthenticated', () => {
    (useProfile as any).mockReturnValue({
      data: null,
      isLoading: false,
      error: null,
    });

    (useAuthContext as any).mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
    });

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('renders children when user is authenticated', () => {
    (useProfile as any).mockReturnValue({
      data: { onboardingCompleted: true },
      isLoading: false,
      error: null,
    });

    (useAuthContext as any).mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
    });

    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });
});
