import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface BaseResponse<T> {
  requestId: string;
  status: boolean;
  message: string;
  data: T;
}

interface UserSummary {
  id: number;
  name: string;
  email: string;
  createdAt: string;
}

interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

interface LoginRequest {
  email: string;
  password: string;
}

interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(payload: LoginRequest): Observable<BaseResponse<LoginResponse>> {
    return this.http.post<BaseResponse<LoginResponse>>(`${this.apiUrl}/login`, payload);
  }

  register(payload: RegisterRequest): Observable<BaseResponse<UserSummary>> {
    return this.http.post<BaseResponse<UserSummary>>(`${this.apiUrl}/register`, payload);
  }

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
