import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable } from 'rxjs';
import { BaseResponse, LoginRequest, LoginResponse, RegisterRequest, UserSummary } from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  login(payload: LoginRequest): Observable<BaseResponse<LoginResponse>> {
    return this.http.post<BaseResponse<LoginResponse>>(`${this.apiUrl}/login`, payload);
  }

  register(payload: RegisterRequest): Observable<BaseResponse<UserSummary>> {
    return this.http.post<BaseResponse<UserSummary>>(`${this.apiUrl}/register`, payload);
  }

  saveToken(token: string): void {
    if (!this.isBrowser()) {
      return;
    }

    localStorage.setItem('token', token);
  }

  saveUser(user: UserSummary): void {
    if (!this.isBrowser()) {
      return;
    }

    localStorage.setItem('user', JSON.stringify(user));
  }

  getCurrentUser(): UserSummary | null {
    if (!this.isBrowser()) {
      return null;
    }

    const rawUser = localStorage.getItem('user');
    if (!rawUser) {
      return null;
    }

    try {
      return JSON.parse(rawUser) as UserSummary;
    } catch {
      localStorage.removeItem('user');
      return null;
    }
  }

  getToken(): string | null {
    if (!this.isBrowser()) {
      return null;
    }

    return localStorage.getItem('token');
  }

  logout(): void {
    if (!this.isBrowser()) {
      return;
    }

    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }
}
