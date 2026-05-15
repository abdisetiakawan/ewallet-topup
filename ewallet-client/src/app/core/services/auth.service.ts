import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, tap, catchError, of, map } from 'rxjs';
import { BaseResponse } from '../models/api.model';
import {
  LoginRequest,
  LoginResponse,
  RefreshResponse,
  RegisterRequest,
  UserSummary,
} from '../models/auth.model';
import { environment } from '../../../environments/environment';
import { inject } from '@angular/core';
import { TokenRefreshService } from './token-refresh.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/api/auth`;
  private readonly tokenRefresh = inject(TokenRefreshService);

  /** Access token stored in memory only — never in localStorage */
  private accessToken: string | null = null;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  login(payload: LoginRequest): Observable<BaseResponse<LoginResponse>> {
    return this.http
      .post<BaseResponse<LoginResponse>>(`${this.apiUrl}/login`, payload, {
        withCredentials: true,
      })
      .pipe(
        tap((response) => {
          this.accessToken = response.data.token;
          this.saveUser(response.data.user);
        })
      );
  }

  register(payload: RegisterRequest): Observable<BaseResponse<UserSummary>> {
    return this.http.post<BaseResponse<UserSummary>>(
      `${this.apiUrl}/register`,
      payload
    );
  }

  /**
   * Attempts to refresh the access token using the HttpOnly cookie.
   * Returns true if refresh was successful, false otherwise.
   */
  refresh(): Observable<boolean> {
    if (!this.isBrowser()) {
      return of(false);
    }

    return this.http
      .post<BaseResponse<RefreshResponse>>(
        `${this.apiUrl}/refresh`,
        {},
        { withCredentials: true }
      )
      .pipe(
        map((response) => {
          this.accessToken = response.data.token;
          return true;
        }),
        catchError(() => {
          this.clearSession();
          return of(false);
        })
      );
  }

  logout(): Observable<BaseResponse<void>> {
    return this.http
      .post<BaseResponse<void>>(
        `${this.apiUrl}/logout`,
        {},
        { withCredentials: true }
      )
      .pipe(
        tap(() => {
          this.tokenRefresh.reset();
          this.clearSession();
        })
      );
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  isLoggedIn(): boolean {
    return !!this.accessToken && !this.isAccessTokenExpired();
  }

  isAccessTokenExpired(): boolean {
    if (!this.accessToken) {
      return true;
    }

    const payload = this.decodeJwtPayload(this.accessToken);
    const exp = payload?.exp;

    if (typeof exp !== 'number') {
      return true;
    }

    return Date.now() >= exp * 1000;
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

  clearSession(): void {
    this.accessToken = null;
    if (this.isBrowser()) {
      localStorage.removeItem('user');
    }
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  private decodeJwtPayload(token: string): { exp?: number } | null {
    const segments = token.split('.');

    if (segments.length !== 3) {
      return null;
    }

    try {
      const payload = this.base64UrlDecode(segments[1]);
      return JSON.parse(payload) as { exp?: number };
    } catch {
      return null;
    }
  }

  private base64UrlDecode(value: string): string {
    const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');

    if (typeof atob === 'function') {
      return atob(padded);
    }

    throw new Error('Base64 decoder is unavailable');
  }
}
