export type UserRole = 'CUSTOMER' | 'ADMIN';

export interface UserSummary {
  id?: number;
  userId?: number;
  name: string;
  email: string;
  createdAt: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

export interface RefreshResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface EmailChangeResponse {
  newEmail: string;
  expiresInMinutes: number;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}
