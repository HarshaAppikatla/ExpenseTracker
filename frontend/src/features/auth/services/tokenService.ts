let memoryAccessToken: string | null = null;

export const tokenService = {
  getAccessToken: (): string | null => {
    return memoryAccessToken;
  },

  setAccessToken: (token: string | null): void => {
    memoryAccessToken = token;
    if (token) {
      document.cookie = `JWT=${token}; path=/; max-age=86400; SameSite=Lax`;
    } else {
      document.cookie = `JWT=; path=/; max-age=0; SameSite=Lax`;
    }
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
    document.cookie = `JWT=; path=/; max-age=0; SameSite=Lax`;
  },

  hasSession: (): boolean => {
    return !!localStorage.getItem('refresh_token');
  }
};
export type TokenServiceType = typeof tokenService;
