import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BaseResponse } from '../models/api.model';
import {
  AdminMerchantConfigPayload,
  AdminMerchantDto,
} from '../models/admin-merchant.model';

@Injectable({
  providedIn: 'root',
})
export class AdminMerchantApiService {
  private readonly apiUrl = `${environment.apiUrl}/api/admin/merchants`;

  constructor(private http: HttpClient) {}

  getAllMerchants(): Observable<BaseResponse<AdminMerchantDto[]>> {
    return this.http.get<BaseResponse<AdminMerchantDto[]>>(this.apiUrl);
  }

  getMerchant(id: number): Observable<BaseResponse<AdminMerchantDto>> {
    return this.http.get<BaseResponse<AdminMerchantDto>>(`${this.apiUrl}/${id}`);
  }

  createMerchant(payload: AdminMerchantConfigPayload): Observable<BaseResponse<AdminMerchantDto>> {
    return this.http.post<BaseResponse<AdminMerchantDto>>(this.apiUrl, payload);
  }

  updateMerchant(
    id: number,
    payload: AdminMerchantConfigPayload
  ): Observable<BaseResponse<AdminMerchantDto>> {
    return this.http.put<BaseResponse<AdminMerchantDto>>(`${this.apiUrl}/${id}`, payload);
  }
}
