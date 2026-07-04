let memoryAccessToken: string | null = null;

export const tokenService = {
  getAccessToken: (): string | null => {
    return memoryAccessToken;
  },

  setAccessToken: (token: string | null): void => {
    memoryAccessToken = token;
  },

  getRefreshToken: (): string | null => {
    return localStorage.getItem('refresh_token');
  },

  setRefreshToken: (token: string | null): void => {
    if (token) {
      localStorage.setItem('refresh_token', token);
    } else {
      localStorage.removeItem('refresh_token');
    }
  },

  removeTokens: (): void => {
    memoryAccessToken = null;
    localStorage.removeItem('refresh_token');
  },

  hasSession: (): boolean => {
    return !!localStorage.getItem('refresh_token');
  }
};
export type TokenServiceType = typeof tokenService;
