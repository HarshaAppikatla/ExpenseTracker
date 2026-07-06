import React, { createContext, useState, useEffect, ReactNode, useCallback, useRef } from 'react';
import axios from 'axios';
import { tokenService } from '@/features/auth/services/tokenService';
import { userService } from '@/features/auth/services/userService';
import { UserDto, LoginResponse } from '@/features/auth/types';
import apiClient from '../api/client';

export interface AuthContextType {
  user: UserDto | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginResponse) => void;
  logout: (reason?: string) => Promise<void>;
  setUser: (user: UserDto | null) => void;
  sessionError: string | null;
  clearSessionError: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

const IDLE_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes
const ABSOLUTE_TIMEOUT_MS = 8 * 60 * 60 * 1000; // 8 hours
const BROADCAST_CHANNEL_NAME = 'expenseflow_auth_sync';

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUserState] = useState<UserDto | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [sessionError, setSessionError] = useState<string | null>(null);

  const idleTimerRef = useRef<NodeJS.Timeout | null>(null);
  const absoluteTimerRef = useRef<NodeJS.Timeout | null>(null);
  const broadcastChannelRef = useRef<BroadcastChannel | null>(null);

  const clearSessionError = useCallback(() => {
    setSessionError(null);
  }, []);

  const setUser = useCallback((userDto: UserDto | null) => {
    setUserState(userDto);
    setIsAuthenticated(!!userDto);
  }, []);

  // Broadcast channel setup
  useEffect(() => {
    const channel = new BroadcastChannel(BROADCAST_CHANNEL_NAME);
    broadcastChannelRef.current = channel;

    channel.onmessage = (event) => {
      if (event.data.type === 'LOGOUT') {
        tokenService.removeTokens();
        setUserState(null);
        setIsAuthenticated(false);
        if (event.data.reason) {
          setSessionError(event.data.reason);
        }
      } else if (event.data.type === 'LOGIN') {
        setUserState(event.data.user);
        setIsAuthenticated(true);
        clearSessionError();
        // Setup timer on login broadcast
        startSessionTimers();
      }
    };

    return () => {
      channel.close();
    };
  }, [clearSessionError]);

  const logout = useCallback(async (reason?: string) => {
    setIsLoading(true);
    try {
      const refreshToken = tokenService.getRefreshToken();
      if (refreshToken) {
        // Suppress errors during logout call to ensure client state is cleared regardless of network
        await apiClient.post('/auth/logout', { refreshToken }).catch(() => {});
      }
    } finally {
      tokenService.removeTokens();
      setUserState(null);
      setIsAuthenticated(false);
      setIsLoading(false);
      
      if (reason) {
        setSessionError(reason);
      }

      // Clear timers
      clearSessionTimers();

      // Notify other tabs
      if (broadcastChannelRef.current) {
        broadcastChannelRef.current.postMessage({ type: 'LOGOUT', reason });
      }
    }
  }, []);

  const login = useCallback((data: LoginResponse) => {
    tokenService.setAccessToken(data.accessToken);
    tokenService.setRefreshToken(data.refreshToken);
    localStorage.setItem('auth_session_start', Date.now().toString());

    setUserState(data.user);
    setIsAuthenticated(true);
    setIsLoading(false);
    clearSessionError();

    // Start session duration timers
    startSessionTimers();

    // Notify other tabs
    if (broadcastChannelRef.current) {
      broadcastChannelRef.current.postMessage({ type: 'LOGIN', user: data.user });
    }
  }, [clearSessionError]);

  const clearSessionTimers = () => {
    if (idleTimerRef.current) clearTimeout(idleTimerRef.current);
    if (absoluteTimerRef.current) clearTimeout(absoluteTimerRef.current);
  };

  const startSessionTimers = useCallback(() => {
    clearSessionTimers();

    // 1. Setup Idle Timeout (30 minutes)
    const resetIdleTimer = () => {
      if (idleTimerRef.current) clearTimeout(idleTimerRef.current);
      idleTimerRef.current = setTimeout(() => {
        logout('Session expired due to inactivity.');
      }, IDLE_TIMEOUT_MS);
    };

    resetIdleTimer();

    // Bind activity listeners
    const activityEvents = ['mousemove', 'keydown', 'scroll', 'touchstart', 'click', 'api-activity'];
    activityEvents.forEach((ev) => {
      window.addEventListener(ev, resetIdleTimer);
    });

    // 2. Setup Absolute Timeout (8 hours)
    const sessionStart = localStorage.getItem('auth_session_start');
    const elapsed = sessionStart ? Date.now() - parseInt(sessionStart, 10) : 0;
    const remainingTime = Math.max(0, ABSOLUTE_TIMEOUT_MS - elapsed);

    absoluteTimerRef.current = setTimeout(() => {
      logout('Session expired. Please log in again.');
    }, remainingTime);

    // Return cleanup helper
    return () => {
      clearSessionTimers();
      activityEvents.forEach((ev) => {
        window.removeEventListener(ev, resetIdleTimer);
      });
    };
  }, [logout]);

  // Handle active listeners cleanup on unmount
  useEffect(() => {
    return () => {
      clearSessionTimers();
    };
  }, []);

  // AuthInitializer logic: restore session silently on startup
  useEffect(() => {
    const initializeAuth = async () => {
      if (tokenService.hasSession()) {
        try {
          const refreshToken = tokenService.getRefreshToken();
          const baseUrl = apiClient.defaults.baseURL || 'http://localhost:8080/api/v1';
          
          // Request token refresh
          const refreshRes = await axios.post(`${baseUrl}/auth/refresh`, {
            refreshToken,
          });

          const { accessToken, refreshToken: newRefreshToken } = refreshRes.data.data;
          tokenService.setAccessToken(accessToken);
          tokenService.setRefreshToken(newRefreshToken);

          // Fetch user profile
          const profile = await userService.getMe();
          setUserState(profile);
          setIsAuthenticated(true);
          
          // Restart timers on successful restoration
          startSessionTimers();
        } catch (error) {
          console.error('Session restoration failed:', error);
          tokenService.removeTokens();
        }
      }
      setIsLoading(false);
    };

    initializeAuth();
  }, [startSessionTimers]);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isLoading,
        login,
        logout,
        setUser,
        sessionError,
        clearSessionError
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
