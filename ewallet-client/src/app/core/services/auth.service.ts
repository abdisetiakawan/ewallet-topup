import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseResponse, LoginRequest, LoginResponse, RegisterRequest, UserSummary } from '../models/auth.model';

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
