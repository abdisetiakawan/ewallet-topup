import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, tap, catchError, of, map } from 'rxjs';
import {
  BaseResponse,
  LoginRequest,
  LoginResponse,
  RefreshResponse,
  RegisterRequest,
  UserSummary,
} from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';

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
          this.clearSession();
        })
      );
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  isLoggedIn(): boolean {
    return !!this.accessToken;
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

  private clearSession(): void {
    this.accessToken = null;
    if (this.isBrowser()) {
      localStorage.removeItem('user');
    }
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }
}
