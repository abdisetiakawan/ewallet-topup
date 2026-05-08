export interface BaseResponse<T> {
  requestId: string;
  status: boolean;
  message: string;
  data: T;
}

export interface UserSummary {
  id: number;
  name: string;
  email: string;
  createdAt: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
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
