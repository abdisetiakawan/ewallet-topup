import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseResponse } from '../models/auth.model';
import { PaymentRequest, PaymentResponse } from '../models/transaction.model';

@Injectable({
  providedIn: 'root',
})
export class TransactionApiService {
  private readonly apiUrl = 'http://localhost:8080/api/transactions';

  constructor(private http: HttpClient) {}

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
