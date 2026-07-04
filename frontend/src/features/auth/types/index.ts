export interface UserDto {
  id: string;
  fullName: string;
  email: string;
  status: 'ACTIVE' | 'PENDING_VERIFICATION' | 'LOCKED' | 'DISABLED';
  avatarUrl: string | null;
  loginProvider: string;
  createdAt: string;
  updatedAt: string;
  roles: string[];
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: UserDto;
  roles: string[];
  permissions: string[];
}

export interface TokenRefreshResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}
