import { useContext } from 'react';
import { render, screen, act } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { AuthProvider, AuthContext } from '../AuthContext';
import { tokenService } from '@/features/auth/services/tokenService';

// Mock token service
vi.mock('@/features/auth/services/tokenService', () => ({
  tokenService: {
    setAccessToken: vi.fn(),
    setRefreshToken: vi.fn(),
    removeTokens: vi.fn(),
    getRefreshToken: vi.fn(),
    hasSession: vi.fn().mockReturnValue(false),
  },
}));

// Test helper component to consume AuthContext
const TestConsumer = () => {
  const context = useContext(AuthContext);
  if (!context) return null;
  return (
    <div>
      <span data-testid="auth-state">{context.isAuthenticated ? 'authenticated' : 'unauthenticated'}</span>
      <span data-testid="user-email">{context.user?.email || 'none'}</span>
      <button data-testid="login-btn" onClick={() => context.login({
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        expiresIn: 900000,
        user: { id: 'user-1', email: 'user@example.com', fullName: 'User', roles: ['ROLE_USER'], status: 'ACTIVE', loginProvider: 'LOCAL' } as any,
        roles: ['ROLE_USER'],
        permissions: []
      })}>Login</button>
      <button data-testid="logout-btn" onClick={() => context.logout('manual')}>Logout</button>
    </div>
  );
};

describe('AuthContext Timers and Tab Broadcast Synchronizations', () => {

  beforeEach(() => {
    vi.useFakeTimers();
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('sets authentication state and triggers BroadcastChannel on login', async () => {
    // Spy on BroadcastChannel constructor and postMessage
    const postMessageSpy = vi.fn();
    const mockChannel = {
      postMessage: postMessageSpy,
      close: vi.fn(),
    };
    const bcConstructorSpy = vi.spyOn(global, 'BroadcastChannel').mockImplementation(() => mockChannel as any);

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    // Initial state
    expect(screen.getByTestId('auth-state').textContent).toBe('unauthenticated');

    // Trigger Login
    fireEventClick('login-btn');

    expect(screen.getByTestId('auth-state').textContent).toBe('authenticated');
    expect(screen.getByTestId('user-email').textContent).toBe('user@example.com');
    expect(tokenService.setAccessToken).toHaveBeenCalledWith('access-token');

    // BroadcastChannel verification
    expect(bcConstructorSpy).toHaveBeenCalledWith('expenseflow_auth_sync');
    expect(postMessageSpy).toHaveBeenCalledWith(expect.objectContaining({ type: 'LOGIN' }));
  });

  it('triggers logout after 30 minutes of idle inactivity', async () => {
    const postMessageSpy = vi.fn();
    const mockChannel = {
      postMessage: postMessageSpy,
      close: vi.fn(),
    };
    vi.spyOn(global, 'BroadcastChannel').mockImplementation(() => mockChannel as any);

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    // Login
    fireEventClick('login-btn');
    expect(screen.getByTestId('auth-state').textContent).toBe('authenticated');

    // Fast-forward 29 minutes (no timeout yet)
    act(() => {
      vi.advanceTimersByTime(29 * 60 * 1000);
    });
    expect(screen.getByTestId('auth-state').textContent).toBe('authenticated');

    // Fast-forward another 2 minutes (exceeds 30 mins limit)
    act(() => {
      vi.advanceTimersByTime(2 * 60 * 1000);
    });

    // Should automatically log out
    expect(screen.getByTestId('auth-state').textContent).toBe('unauthenticated');
    expect(tokenService.removeTokens).toHaveBeenCalled();
  });

  it('logs out across all tabs when receiving LOGOUT broadcast event', async () => {
    let messageListener: ((ev: MessageEvent) => any) | null = null;
    const mockChannel = {
      postMessage: vi.fn(),
      close: vi.fn(),
      set onmessage(val: any) {
        messageListener = val;
      },
      get onmessage() {
        return messageListener;
      }
    };
    vi.spyOn(global, 'BroadcastChannel').mockImplementation(() => mockChannel as any);

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    // Login
    fireEventClick('login-btn');
    expect(screen.getByTestId('auth-state').textContent).toBe('authenticated');

    // Simulate LOGOUT message received from another tab
    act(() => {
      if (messageListener) {
        messageListener(new MessageEvent('message', {
          data: { type: 'LOGOUT', reason: 'Session expired' }
        }));
      }
    });

    // Local tab should log out
    expect(screen.getByTestId('auth-state').textContent).toBe('unauthenticated');
  });

  // Helper utility
  function fireEventClick(id: string) {
    const btn = screen.getByTestId(id);
    act(() => {
      btn.click();
    });
  }
});
