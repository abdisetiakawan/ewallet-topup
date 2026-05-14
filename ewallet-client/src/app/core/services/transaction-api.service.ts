import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseResponse } from '../models/api.model';
import {
  PaymentRequest,
  PaymentResponse,
  TransactionHistoryQuery,
  TransactionHistoryResponse,
} from '../models/transaction.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TransactionApiService {
  private readonly apiUrl = `${environment.apiUrl}/api/transactions`;

  constructor(private http: HttpClient) {}

  getTransactions(
    query: TransactionHistoryQuery = {}
  ): Observable<BaseResponse<TransactionHistoryResponse>> {
    let params = new HttpParams()
      .set('page', (query.page ?? 0).toString())
      .set('size', (query.size ?? 10).toString());

    if (query.status) {
      params = params.set('status', query.status);
    }

    if (query.type) {
      params = params.set('type', query.type);
    }

    return this.http.get<BaseResponse<TransactionHistoryResponse>>(this.apiUrl, { params });
  }

  pay(
    payload: PaymentRequest,
    idempotencyKey: string
  ): Observable<BaseResponse<PaymentResponse>> {
    return this.http.post<BaseResponse<PaymentResponse>>(
      `${this.apiUrl}/pay`,
      payload,
      {
        headers: {
          'Idempotency-Key': idempotencyKey,
        },
      }
    );
  }
}
