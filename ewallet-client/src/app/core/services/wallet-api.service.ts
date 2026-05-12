import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseResponse } from '../models/auth.model';
import { TopupRequest, TopupResponse, WalletBalanceResponse } from '../models/wallet.model';

@Injectable({
  providedIn: 'root',
})
export class WalletApiService {
  private readonly apiUrl = 'http://localhost:8080/api/wallet';

  constructor(private http: HttpClient) {}

  getBalance(): Observable<BaseResponse<WalletBalanceResponse>> {
    return this.http.get<BaseResponse<WalletBalanceResponse>>(`${this.apiUrl}/balance`);
  }

  topup(
    payload: TopupRequest,
    idempotencyKey: string
  ): Observable<BaseResponse<TopupResponse>> {
    return this.http.post<BaseResponse<TopupResponse>>(
      `${this.apiUrl}/topup`,
      payload,
      {
        headers: {
          'Idempotency-Key': idempotencyKey,
        },
      }
    );
  }
}
