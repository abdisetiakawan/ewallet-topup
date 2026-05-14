import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BaseResponse } from '../models/api.model';
import { ChangePasswordRequest, EmailChangeResponse, UserSummary } from '../models/auth.model';

export interface UpdateProfileRequest {
  name: string;
}

export interface EmailChangeRequest {
  newEmail: string;
}

export interface EmailChangeConfirmRequest {
  token: string;
}

export type { ChangePasswordRequest };

@Injectable({
  providedIn: 'root',
})
export class UserApiService {
  private readonly apiUrl = `${environment.apiUrl}/api/users`;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<BaseResponse<UserSummary>> {
    return this.http.get<BaseResponse<UserSummary>>(`${this.apiUrl}/me`);
  }

  updateProfile(payload: UpdateProfileRequest): Observable<BaseResponse<UserSummary>> {
    return this.http.put<BaseResponse<UserSummary>>(`${this.apiUrl}/me`, payload);
  }

  requestEmailChange(payload: EmailChangeRequest): Observable<BaseResponse<EmailChangeResponse>> {
    return this.http.post<BaseResponse<EmailChangeResponse>>(`${this.apiUrl}/me/email`, payload);
  }

  confirmEmailChange(payload: EmailChangeConfirmRequest): Observable<BaseResponse<UserSummary>> {
    return this.http.post<BaseResponse<UserSummary>>(`${this.apiUrl}/me/email/confirm`, payload);
  }

  changePassword(payload: ChangePasswordRequest): Observable<BaseResponse<void>> {
    return this.http.put<BaseResponse<void>>(`${this.apiUrl}/me/password`, payload);
  }
}
